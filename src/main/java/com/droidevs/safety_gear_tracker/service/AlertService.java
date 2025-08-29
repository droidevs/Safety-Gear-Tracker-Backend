package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AlertRequestDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertSummaryResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface AlertService {
    AlertResponseDto createAlert(AlertRequestDto alertRequestDto);
    Page<AlertSummaryResponseDto> getAllAlerts(int page, int size);
    Optional<AlertResponseDto> getAlertById(Long id);
    byte[] getAlertScreenshot(Long id);
    InputStreamResource getAlertScreenshotStream(Long id);
    InputStreamResource getAlertRecordingStream(Long id);
}
