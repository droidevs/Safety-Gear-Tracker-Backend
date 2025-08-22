package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.model.Recording;
import com.droidevs.safety_gear_tracker.service.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    @Autowired
    private RecordingService recordingService;

    @GetMapping
    public ResponseEntity<Page<Recording>> getAllRecordings(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<Recording> recordings = recordingService.getAllRecordings(page, size);
        return ResponseEntity.ok(recordings);
    }

    @GetMapping("/{recordingId}")
    public ResponseEntity<Recording> getRecordingById(@PathVariable Long recordingId) {
        return recordingService.getRecordingById(recordingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{recordingId}/file")
    public ResponseEntity<byte[]> getRecordingFile(@PathVariable Long recordingId) {
        byte[] file = recordingService.getRecordingFile(recordingId);
        if (file != null) {
            // Assuming video files are typically MP4, adjust if other formats are expected
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("video/mp4")).body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{recordingId}/stream")
    public ResponseEntity<StreamingResponseBody> streamRecordingFile(@PathVariable Long recordingId) {
        InputStreamResource stream = recordingService.getRecordingFileStream(recordingId);
        if (stream != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(outputStream -> FileCopyUtils.copy(stream.getInputStream(), outputStream));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{recordingId}")
    public ResponseEntity<Void> deleteRecording(@PathVariable Long recordingId) {
        boolean deleted = recordingService.deleteRecording(recordingId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
