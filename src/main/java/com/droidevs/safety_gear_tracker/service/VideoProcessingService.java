package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.processors.VideoProcessor;
import com.droidevs.safety_gear_tracker.repository.AlertRepository;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class VideoProcessingService {

    private final Map<Long, VideoProcessor> activeProcessors = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final SafetyGearDetectionService safetyGearDetectionService;
    private final AlertRepository alertRepository;
    private final S3Service s3Service;
    private final RecordingService recordingService;
    private final CameraRepository cameraRepository;
    private final RecordingRepository recordingRepository;
    private final ImageOverlayService imageOverlayService;

    public VideoProcessingService(SafetyGearDetectionService safetyGearDetectionService, AlertRepository alertRepository, S3Service s3Service, RecordingService recordingService, CameraRepository cameraRepository, RecordingRepository recordingRepository, ImageOverlayService imageOverlayService) {
        this.safetyGearDetectionService = safetyGearDetectionService;
        this.alertRepository = alertRepository;
        this.s3Service = s3Service;
        this.recordingService = recordingService;
        this.cameraRepository = cameraRepository;
        this.recordingRepository = recordingRepository;
        this.imageOverlayService = imageOverlayService;
    }

    public void initializeActiveCameras() {
        List<Camera> activeCameras = cameraRepository.findAll()
                .stream()
                .filter(Camera::isActive)
                .collect(Collectors.toList());

        for (Camera camera : activeCameras) {
            startProcessing(camera);
        }
    }

    public void startProcessing(Camera camera) {
        if (camera.isActive() && !activeProcessors.containsKey(camera.getId())) {
            VideoProcessor processor = new VideoProcessor(camera, safetyGearDetectionService, alertRepository, s3Service, recordingService, recordingRepository, imageOverlayService);
            activeProcessors.put(camera.getId(), processor);
            executorService.submit(processor);
            System.out.println("Started video processing for camera: " + camera.getId());
        }
    }

    public void stopProcessing(Long cameraId) {
        VideoProcessor processor = activeProcessors.remove(cameraId);
        if (processor != null) {
            processor.stop();
            System.out.println("Stopped video processing for camera: " + cameraId);
        }
    }

    public Flux<byte[]> getCameraFeed(Long cameraId) {
        VideoProcessor processor = activeProcessors.get(cameraId);
        if (processor != null) {
            return processor.getFrameFlux();
        }
        return Flux.empty();
    }
}
