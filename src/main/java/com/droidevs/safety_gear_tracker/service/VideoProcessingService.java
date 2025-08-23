package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.Camera;
import reactor.core.publisher.Flux;

public interface VideoProcessingService {
    void initializeActiveCameras();
    void startProcessing(Camera camera);
    void stopProcessing(Long cameraId);
    Flux<byte[]> getCameraFeed(Long cameraId);
}
