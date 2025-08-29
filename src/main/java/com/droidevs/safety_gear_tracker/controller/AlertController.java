package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.AlertPagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertSummaryResponseDto;
import com.droidevs.safety_gear_tracker.service.AlertService;
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
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<AlertPagingResponseDto> getAllAlerts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<AlertSummaryResponseDto> alerts = alertService.getAllAlerts(page, size);
        return ResponseEntity.ok(new AlertPagingResponseDto(alerts));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResponseDto> getAlertById(@PathVariable("alertId") Long alertId) {
        return alertService.getAlertById(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{alertId}/screenshot")
    public ResponseEntity<byte[]> getAlertScreenshot(@PathVariable("alertId") Long alertId) {
        byte[] screenshot = alertService.getAlertScreenshot(alertId);
        if (screenshot != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(screenshot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{alertId}/screenshot/stream")
    public ResponseEntity<StreamingResponseBody> streamAlertScreenshot(@PathVariable("alertId") Long alertId) {
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
    public ResponseEntity<StreamingResponseBody> streamAlertRecording(@PathVariable("alertId") Long alertId) {
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
