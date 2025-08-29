package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AlertRequestDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertSummaryResponseDto;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.mappers.AlertMapper;
import com.droidevs.safety_gear_tracker.model.Alert;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Recording;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.AlertRepository;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final S3Service s3Service;
    private final CameraRepository cameraRepository;
    private final RecordingRepository recordingRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    @Override
    @Transactional
    public AlertResponseDto createAlert(AlertRequestDto alertRequestDto) {
        Camera camera = cameraRepository.findById(alertRequestDto.cameraId())
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + alertRequestDto.cameraId()));

        String screenshotKey = null;
        if (alertRequestDto.screenshot() != null && !alertRequestDto.screenshot().isEmpty()) {
            try {
                LocalDateTime now = LocalDateTime.now();
                String dateFolder = now.format(DATE_FORMATTER);
                String timestampFile = now.format(TIMESTAMP_FORMATTER) + ".jpg";
                screenshotKey = String.format("camera_screenshots/%s/%s/%s", camera.getId(), dateFolder, timestampFile);
                s3Service.uploadFile(screenshotKey, alertRequestDto.screenshot().getInputStream());
            } catch (IOException e) {
                // This will be handled by the GlobalExceptionHandler
                throw new RuntimeException("Error uploading screenshot", e);
            }
        }

        LocalDateTime alertTimestamp = LocalDateTime.now();
        Optional<Recording> latestRecording = recordingRepository.findLastRecordingBeforeTimestamp(camera.getId(), alertTimestamp);

        Alert alert = new Alert();
        alert.setCamera(camera);
        alert.setCameraName(camera.getName());
        alert.setDescription(alertRequestDto.description());
        alert.setTimestamp(alertTimestamp);
        alert.setScreenshotUrl(screenshotKey);
        latestRecording.ifPresent(alert::setRecording);

        Alert savedAlert = alertRepository.save(alert);
        return alertMapper.toDto(savedAlert);
    }

    @Override
    public Page<AlertSummaryResponseDto> getAllAlerts(int page, int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Long> cameraIds = getAccessibleCameraIds(user);

        Page<Alert> alertPage = alertRepository.findByCameraIdIn(cameraIds, PageRequest.of(page, size));
        return alertPage.map(alertMapper::toSummaryDto);
    }

    @Override
    public Optional<AlertResponseDto> getAlertById(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Long> accessibleCameraIds = getAccessibleCameraIdsAsSet(user);

        return alertRepository.findById(id)
                .filter(alert -> accessibleCameraIds.contains(alert.getCamera().getId()))
                .map(alertMapper::toDto);
    }

    @Override
    public byte[] getAlertScreenshot(Long id) {
        Alert alert = getAndVerifyAlertAccess(id);
        if (alert.getScreenshotUrl() == null) {
            return null;
        }
        return s3Service.downloadFile(alert.getScreenshotUrl());
    }

    @Override
    public InputStreamResource getAlertScreenshotStream(Long id) {
        Alert alert = getAndVerifyAlertAccess(id);
        if (alert.getScreenshotUrl() == null) {
            return null;
        }
        ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(alert.getScreenshotUrl());
        return new InputStreamResource(s3Object);
    }

    @Override
    public InputStreamResource getAlertRecordingStream(Long id) {
        Alert alert = getAndVerifyAlertAccess(id);
        if (alert.getRecording() == null || alert.getRecording().getFilePath() == null) {
            return null;
        }
        ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(alert.getRecording().getFilePath());
        return new InputStreamResource(s3Object);
    }

    private Alert getAndVerifyAlertAccess(Long alertId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Long> accessibleCameraIds = getAccessibleCameraIdsAsSet(user);

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        if (!accessibleCameraIds.contains(alert.getCamera().getId())) {
            throw new ResourceNotFoundException("Alert not found with ID: " + alertId);
        }
        return alert;
    }

    private List<Long> getAccessibleCameraIds(User user) {
        return user.getZones().stream()
                .flatMap(zone -> zone.getCameras().stream())
                .map(Camera::getId)
                .collect(Collectors.toList());
    }
    
    private Set<Long> getAccessibleCameraIdsAsSet(User user) {
        return user.getZones().stream()
                .flatMap(zone -> zone.getCameras().stream())
                .map(Camera::getId)
                .collect(Collectors.toSet());
    }
}
