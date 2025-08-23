package com.droidevs.safety-gear-tracker.service;

import com.droidevs.safety-gear-tracker.model.Camera;
import reactor.core.publisher.Flux;

public interface VideoProcessingService {
    void initializeActiveCameras();
    void startProcessing(Camera camera);
    void stopProcessing(Long cameraId);
    Flux<byte[]> getCameraFeed(Long cameraId);
}
