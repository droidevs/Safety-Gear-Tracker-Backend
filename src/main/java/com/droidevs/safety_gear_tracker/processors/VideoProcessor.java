package com.droidevs.safety_gear_tracker.processors;

import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import com.droidevs.safety_gear_tracker.handler.exception.S3OperationException;
import com.droidevs.safety_gear_tracker.model.Alert;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Recording;
import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.droidevs.safety_gear_tracker.repository.AlertRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import com.droidevs.safety_gear_tracker.service.ImageOverlayService;
import com.droidevs.safety_gear_tracker.service.S3Service;
import com.droidevs.safety_gear_tracker.service.SafetyGearDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class VideoProcessor implements Runnable {

    private final Camera camera;
    private final SafetyGearDetectionService safetyGearDetectionService;
    private final AlertRepository alertRepository;
    private final S3Service s3Service;
    private final RecordingRepository recordingRepository;
    private final ImageOverlayService imageOverlayService;
    private final Flux<byte[]> frameFlux;
    private FluxSink<byte[]> frameSink;

    private volatile boolean running = true;
    private static final int FRAME_SKIP_AI = 30;

    /**
     * BUG-22 FIX: the original {@code Flux.create(...).publish().autoConnect()}
     * chain emits frames at camera speed with no backpressure strategy.  A slow
     * consumer (e.g. a browser over a flaky connection) cannot signal demand fast
     * enough and the unbounded internal queue fills up, causing OutOfMemoryError.
     *
     * Fix: replace {@code Flux.create} with an overflow strategy of
     * {@link FluxSink.OverflowStrategy#DROP} so that frames are discarded rather
     * than queued when the downstream cannot keep up.  For a live video feed
     * dropping stale frames is the correct semantic — the consumer always wants
     * the *latest* frame, not a backlog of old ones.
     *
     * We also keep {@code .publish().autoConnect()} so that multiple subscribers
     * (e.g. several browser tabs) share a single capture loop without restarting
     * the camera stream.
     */
    public VideoProcessor(
            Camera camera,
            SafetyGearDetectionService safetyGearDetectionService,
            AlertRepository alertRepository,
            S3Service s3Service,
            RecordingRepository recordingRepository,
            ImageOverlayService imageOverlayService) {
        this.camera = camera;
        this.safetyGearDetectionService = safetyGearDetectionService;
        this.alertRepository = alertRepository;
        this.s3Service = s3Service;
        this.recordingRepository = recordingRepository;
        this.imageOverlayService = imageOverlayService;

        // BUG-22 FIX: use DROP overflow strategy instead of the default BUFFER
        // (which is unbounded).  Stale video frames are worthless to clients;
        // dropping them under load is the correct behaviour for a live feed.
        this.frameFlux = Flux.<byte[]>create(
                        sink -> this.frameSink = sink,
                        FluxSink.OverflowStrategy.DROP)
                .publish()
                .autoConnect();
    }

    public Flux<byte[]> getFrameFlux() {
        return this.frameFlux;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        VideoCapture cap = new VideoCapture(camera.getStreamUrl());
        if (!cap.isOpened()) {
            if (this.frameSink != null) {
                this.frameSink.error(new IOException(
                        "Cannot open camera stream: " + camera.getStreamUrl()));
            }
            log.error("Failed to open camera stream for camera {}: {}",
                    camera.getId(), camera.getStreamUrl());
            running = false;
            return;
        }

        Mat frame = new Mat();
        int frameCount = 0;
        try {
            while (running && cap.read(frame)) {
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, matOfByte);
                byte[] imageData = matOfByte.toArray();

                // BUG-22 FIX: the DROP strategy on the Flux means next() is a
                // no-op when the downstream is full — no queue growth possible.
                if (frameSink != null && !frameSink.isCancelled()) {
                    frameSink.next(imageData);
                }

                if (frameCount % FRAME_SKIP_AI == 0) {
                    try {
                        List<SafetyViolation> violations =
                                safetyGearDetectionService.findViolations(imageData, camera);
                        if (!violations.isEmpty()) {
                            handleViolations(violations, imageData);
                        }
                    } catch (Exception e) {
                        log.error("Error during violation detection for camera {}: {}",
                                camera.getId(), e.getMessage(), e);
                    }
                }
                frameCount++;
            }
        } catch (Exception e) {
            log.error("Unhandled exception in VideoProcessor for camera {}: {}",
                    camera.getId(), e.getMessage(), e);
            if (frameSink != null) {
                frameSink.error(e);
            }
        } finally {
            cap.release();
            if (frameSink != null && !frameSink.isCancelled()) {
                frameSink.complete();
            }
            running = false;
            log.info("Video processing stopped for camera: {}", camera.getId());
        }
    }

    public void stop() {
        running = false;
    }

    private void handleViolations(List<SafetyViolation> violations, byte[] originalImage) {
        byte[] annotatedImage = imageOverlayService.drawViolationsOnImage(originalImage, violations);
        String snapshotFileName = "snapshot-" + UUID.randomUUID() + ".jpg";
        try {
            s3Service.uploadFile(snapshotFileName, new ByteArrayInputStream(annotatedImage));
            LocalDateTime timestamp = LocalDateTime.now();

            Optional<Recording> lastRecording =
                    recordingRepository.findLastRecordingBeforeTimestamp(camera.getId(), timestamp);

            if (lastRecording.isEmpty()) {
                log.warn("No recording found for camera {} before {}. Skipping alert.",
                        camera.getId(), timestamp);
                return;
            }

            for (SafetyViolation violation : violations) {
                String missingGearString = violation.missingGear().stream()
                        .map(SafetyGearType::name)
                        .collect(Collectors.joining(", "));

                Alert alert = new Alert();
                alert.setCamera(camera);
                alert.setTimestamp(timestamp);
                alert.setDescription("Missing safety gear: " + missingGearString);
                alert.setScreenshotUrl(snapshotFileName);
                alert.setRecording(lastRecording.get());
                alertRepository.save(alert);

                log.info("Alert created for camera {} — missing: {}",
                        camera.getId(), missingGearString);
            }
        } catch (S3OperationException e) {
            log.error("Failed to upload snapshot or create alert for camera {}: {}",
                    camera.getId(), e.getMessage(), e);
        }
    }
}