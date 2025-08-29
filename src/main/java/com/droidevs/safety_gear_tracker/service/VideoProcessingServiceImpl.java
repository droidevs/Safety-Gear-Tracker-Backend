package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.processors.VideoProcessor;
import com.droidevs.safety_gear_tracker.repository.AlertRepository;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final Map<Long, VideoProcessor> activeProcessors = new ConcurrentHashMap<>();
    private final SafetyGearDetectionService safetyGearDetectionService;
    private final AlertRepository alertRepository;
    private final S3Service s3Service;
    private final RecordingRepository recordingRepository;
    private final ImageOverlayService imageOverlayService;
    private final CameraRepository cameraRepository;
    private final Executor taskExecutor;

    // Manual constructor with @Qualifier for Executor to resolve ambiguity
    public VideoProcessingServiceImpl(
            SafetyGearDetectionService safetyGearDetectionService,
            AlertRepository alertRepository,
            S3Service s3Service,
            RecordingService recordingService,
            RecordingRepository recordingRepository,
            ImageOverlayService imageOverlayService,
            CameraRepository cameraRepository,
            @Qualifier("taskExecutor") Executor taskExecutor) {
        this.safetyGearDetectionService = safetyGearDetectionService;
        this.alertRepository = alertRepository;
        this.s3Service = s3Service;
        this.recordingRepository = recordingRepository;
        this.imageOverlayService = imageOverlayService;
        this.cameraRepository = cameraRepository;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    public void initializeActiveCameras() {
        log.info("Initializing active cameras for video processing...");
        List<Camera> activeCameras = cameraRepository.findAllByActiveTrue();

        for (Camera camera : activeCameras) {
            try {
                startProcessing(camera);
            } catch (Exception e) {
                log.error("Failed to start video processing for camera {}: {}", camera.getId(), e.getMessage(), e);
            }
        }
        log.info("Finished initializing active cameras. {} processors started.", activeProcessors.size());
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void checkAndRestartVideoProcessors() {
        log.debug("Scheduled check for video processors started.");
        List<Camera> allActiveCamerasInDb = cameraRepository.findAllByActiveTrue();
        Set<Long> activeCameraIdsInDb = allActiveCamerasInDb.stream()
                .map(Camera::getId)
                .collect(Collectors.toSet());

        // Stop processors for cameras that are no longer active in DB or explicitly stopped
        activeProcessors.entrySet().removeIf(entry -> {
            Long cameraId = entry.getKey();
            VideoProcessor processor = entry.getValue();
            // If camera is not in DB's active list, or processor has stopped internally
            if (!activeCameraIdsInDb.contains(cameraId) || !processor.isRunning()) {
                log.info("Stopping video processor for camera {} (no longer active or stopped internally).", cameraId);
                processor.stop(); // Ensure stop() is called
                return true; // Remove from map
            }
            return false;
        });

        // Start processors for active cameras that are not yet running
        for (Camera camera : allActiveCamerasInDb) {
            if (!activeProcessors.containsKey(camera.getId())) {
                try {
                    log.info("Attempting to start video processing for camera {} (detected as inactive or crashed).", camera.getId());
                    startProcessing(camera);
                } catch (Exception e) {
                    log.error("Failed to start video processing for camera {}: {}", camera.getId(), e.getMessage(), e);
                    // Optional: Update camera status in DB to reflect processing failure
                    // For example, if you had a CameraStatus enum and a setter:
                    // camera.setStatus(CameraStatus.PROCESSING_FAILED);
                    // cameraRepository.save(camera); // Save the updated status
                }
            }
        }
        log.debug("Scheduled check for video processors completed. {} processors active.", activeProcessors.size());
    }

    @Override
    public void startProcessing(Camera camera) {
        // Only start if camera is active AND there isn't already a processor for it
        if (camera.isActive() && !activeProcessors.containsKey(camera.getId())) {
            VideoProcessor processor = new VideoProcessor(
                    camera,
                    safetyGearDetectionService,
                    alertRepository,
                    s3Service,
                    recordingRepository,
                    imageOverlayService
                    );
            activeProcessors.put(camera.getId(), processor);
            taskExecutor.execute(processor);
            log.info("Started video processing for camera: {}", camera.getId());
        } else if (!camera.isActive()) {
            log.debug("Attempted to start processing for inactive camera: {}", camera.getId());
        } else {
            log.debug("Video processing already active for camera: {}", camera.getId());
        }
    }

    @Override
    public void stopProcessing(Long cameraId) {
        VideoProcessor processor = activeProcessors.remove(cameraId);
        if (processor != null) {
            processor.stop();
            log.info("Stopped video processing for camera: {}", cameraId);
        }
        else {
            log.warn("Attempted to stop non-existent or already stopped video processor for camera: {}", cameraId);
        }
    }

    @Override
    public Flux<byte[]> getCameraFeed(Long cameraId) {
        VideoProcessor processor = activeProcessors.get(cameraId);
        if (processor != null) {
            return processor.getFrameFlux();
        }
        return Flux.empty();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down VideoProcessingService. Stopping all active video processors...");
        activeProcessors.forEach((cameraId, processor) -> {
            processor.stop();
            log.info("Stopped video processor for camera: {}", cameraId);
        });
        activeProcessors.clear();
        log.info("All video processors stopped.");
    }
}
