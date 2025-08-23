package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AlertRequestDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.Optional;

public interface AlertService {
    AlertResponseDto createAlert(AlertRequestDto alertRequestDto) throws IOException;
    Page<AlertResponseDto> getAllAlerts(int page, int size);
    Optional<AlertResponseDto> getAlertById(Long alertId);
    byte[] getAlertScreenshot(Long alertId);
    InputStreamResource getAlertScreenshotStream(Long alertId);
    InputStreamResource getAlertRecordingStream(Long alertId);
}
