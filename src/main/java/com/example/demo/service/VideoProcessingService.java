package com.example.demo.service;

import com.example.demo.model.Camera;
import com.example.demo.model.SafetyGearType; // Import SafetyGearType
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Collections; // Import Collections (for emptyList placeholder)

// Placeholder imports for video processing and YOLO (replace with actual library imports)
// import org.opencv.core.Mat;
// import org.opencv.videoio.VideoCapture;

@Service
public class VideoProcessingService {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, VideoProcessor> activeProcessors = new ConcurrentHashMap<>();

    public void startProcessing(Camera camera) {
        if (camera.isActive() && !activeProcessors.containsKey(camera.getId())) {
            VideoProcessor processor = new VideoProcessor(camera);
            activeProcessors.put(camera.getId(), processor);
            executorService.submit(processor); // Run processor in a separate thread
            System.out.println("Started video processing for camera: " + camera.getId());
        }
    }

    public void stopProcessing(String cameraId) {
        VideoProcessor processor = activeProcessors.remove(cameraId);
        if (processor != null) {
            processor.stop(); // Signal the processor to stop
            System.out.println("Stopped video processing for camera: " + cameraId);
        }
    }

    // Inner class to handle video processing for a single camera
    // Inside VideoProcessingService.java

    // Don't forget to shut down the executor service when the application stops
    public void shutdown() {
        executorService.shutdownNow();
    }
}