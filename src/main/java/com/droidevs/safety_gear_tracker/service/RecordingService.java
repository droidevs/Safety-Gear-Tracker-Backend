package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Recording;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface RecordingService {
    void startRecording(Camera camera);
    void stopRecording(Long cameraId);
    void recordAllActiveCameras();
    Page<Recording> getAllRecordings(int page, int size);
    Optional<Recording> getRecordingById(Long id);
    byte[] getRecordingFile(Long id);
    InputStreamResource getRecordingFileStream(Long id);
    void deleteRecording(Long id); // Changed return type to void
}
