package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<Page<AlertResponseDto>> getAllAlerts(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        Page<AlertResponseDto> alerts = alertService.getAllAlerts(page, size);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResponseDto> getAlertById(@PathVariable Long alertId) {
        return alertService.getAlertById(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{alertId}/screenshot")
    public ResponseEntity<byte[]> getAlertScreenshot(@PathVariable Long alertId) {
        byte[] screenshot = alertService.getAlertScreenshot(alertId);
        if (screenshot != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(screenshot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{alertId}/screenshot/stream")
    public ResponseEntity<StreamingResponseBody> streamAlertScreenshot(@PathVariable Long alertId) {
        InputStreamResource stream = alertService.getAlertScreenshotStream(alertId);
        if (stream != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(outputStream -> FileCopyUtils.copy(stream.getInputStream(), outputStream));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{alertId}/recording/stream")
    public ResponseEntity<StreamingResponseBody> streamAlertRecording(@PathVariable Long alertId) {
        InputStreamResource stream = alertService.getAlertRecordingStream(alertId);
        if (stream != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(outputStream -> FileCopyUtils.copy(stream.getInputStream(), outputStream));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
