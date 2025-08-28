package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AlertRequestDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.handler.exception.S3OperationException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final S3Service s3Service;
    private final CameraRepository cameraRepository;
    private final RecordingRepository recordingRepository;
    private final AlertMapper alertMapper;

    @Override
    @Transactional
    public AlertResponseDto createAlert(AlertRequestDto alertRequestDto) {
        Alert alert = new Alert();
        alert.setCameraName(alertRequestDto.cameraName());
        alert.setDescription(alertRequestDto.description());
        alert.setTimestamp(LocalDateTime.now());

        String screenshotKey = "screenshots/" + UUID.randomUUID() + "-" + alertRequestDto.screenshot().getOriginalFilename();
        try {
            s3Service.uploadFile(screenshotKey, alertRequestDto.screenshot().getInputStream());
        } catch (IOException e) { // IOException from alertRequestDto.screenshot().getInputStream()
            throw new S3OperationException("Failed to get input stream for screenshot for alert: " + alertRequestDto.cameraName(), e);
        } catch (S3OperationException e) { // S3OperationException from s3Service.uploadFile
            // Re-throw with more specific context for Alert creation
            throw new S3OperationException("Failed to upload alert screenshot to S3 for camera: " + alertRequestDto.cameraName(), e);
        }
        alert.setScreenshotUrl(screenshotKey);

        Optional<Camera> cameraOptional = cameraRepository.findByName(alertRequestDto.cameraName());
        if (cameraOptional.isPresent()) {
            Camera camera = cameraOptional.get();
            Optional<Recording> recordingOptional = recordingRepository.findLastRecordingBeforeTimestamp(camera.getId(), alert.getTimestamp());
            recordingOptional.ifPresent(alert::setRecording);
        }

        Alert savedAlert = alertRepository.save(alert);
        return alertMapper.toDto(savedAlert);
    }

    @Override
    public Page<AlertResponseDto> getAllAlerts(int page, int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        List<AlertResponseDto> alerts = alertRepository.findAll(pageable).stream()
                .filter(alert -> user.getZones().contains(alert.getCamera().getZone()))
                .map(alertMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(alerts, pageable, alerts.size());
    }

    @Override
    public Optional<AlertResponseDto> getAlertById(Long alertId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return alertRepository.findById(alertId)
                .filter(alert -> user.getZones().contains(alert.getCamera().getZone()))
                .map(alertMapper::toDto);
    }

    @Override
    public byte[] getAlertScreenshot(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));
        try {
            return s3Service.downloadFile(alert.getScreenshotUrl());
        } catch (S3OperationException e) { 
            throw new S3OperationException("Failed to download alert screenshot from S3 for alert ID: " + alertId, e);
        }
    }

    @Override
    public InputStreamResource getAlertScreenshotStream(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));
        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(alert.getScreenshotUrl());
            return new InputStreamResource(s3Object);
        } catch (S3OperationException e) { 
            throw new S3OperationException("Error streaming alert screenshot from S3 for alert ID: " + alertId, e);
        }
    }

    @Override
    public InputStreamResource getAlertRecordingStream(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        Recording recording = alert.getRecording();
        if (recording == null || recording.getFilePath() == null || recording.getFilePath().isEmpty()) {
            throw new ResourceNotFoundException("No recording found for alert ID: " + alertId);
        }

        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(recording.getFilePath());
            return new InputStreamResource(s3Object);
        } catch (S3OperationException e) {
            throw new S3OperationException("Error streaming alert recording from S3 for alert ID: " + alertId, e);
        }
    }
}
