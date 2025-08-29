package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.RecordingPagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.RecordingResponseDto;
import com.droidevs.safety_gear_tracker.service.RecordingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/recordings")
@RequiredArgsConstructor
@Tag(name = "Recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping
    public ResponseEntity<RecordingPagingResponseDto> getAllRecordings(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<RecordingResponseDto> recordings = recordingService.getAllRecordings(page, size);
        return ResponseEntity.ok(new RecordingPagingResponseDto(recordings));
    }

    @GetMapping("/{recordingId}")
    public ResponseEntity<RecordingResponseDto> getRecordingById(@PathVariable("recordingId") Long recordingId) {
        return recordingService.getRecordingById(recordingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{recordingId}/file")
    public ResponseEntity<byte[]> getRecordingFile(@PathVariable("recordingId") Long recordingId) {
        byte[] file = recordingService.getRecordingFile(recordingId);
        if (file != null) {
            // Assuming video files are typically MP4, adjust if other formats are expected
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("video/mp4")).body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{recordingId}/stream")
    public ResponseEntity<StreamingResponseBody> streamRecordingFile(@PathVariable("recordingId") Long recordingId) {
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
    public ResponseEntity<Void> deleteRecording(@PathVariable("recordingId") Long recordingId) {
        recordingService.deleteRecording(recordingId);
        return ResponseEntity.noContent().build();
    }
}
