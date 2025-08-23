package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AlertRequestDto;
import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
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
    public AlertResponseDto createAlert(AlertRequestDto alertRequestDto) throws IOException {
        Alert alert = new Alert();
        alert.setCameraName(alertRequestDto.cameraName());
        alert.setDescription(alertRequestDto.description());
        alert.setTimestamp(LocalDateTime.now());

        String screenshotKey = "screenshots/" + UUID.randomUUID() + "-" + alertRequestDto.screenshot().getOriginalFilename();
        s3Service.uploadFile(screenshotKey, alertRequestDto.screenshot().getInputStream());
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
        Optional<Alert> alert = alertRepository.findById(alertId);
        if (alert.isPresent()) {
            try {
                return s3Service.downloadFile(alert.get().getScreenshotUrl());
            } catch (IOException e) {
                System.err.println("Error downloading alert screenshot from S3: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public InputStreamResource getAlertScreenshotStream(Long alertId) {
        Optional<Alert> alertOptional = alertRepository.findById(alertId);
        if (alertOptional.isPresent()) {
            Alert alert = alertOptional.get();
            try {
                ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(alert.getScreenshotUrl());
                return new InputStreamResource(s3Object);
            } catch (Exception e) {
                System.err.println("Error streaming alert screenshot from S3: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public InputStreamResource getAlertRecordingStream(Long alertId) {
        Optional<Alert> alertOptional = alertRepository.findById(alertId);
        if (alertOptional.isPresent()) {
            Alert alert = alertOptional.get();
            Recording recording = alert.getRecording();
            if (recording != null && recording.getFilePath() != null && !recording.getFilePath().isEmpty()) {
                try {
                    ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(recording.getFilePath());
                    return new InputStreamResource(s3Object);
                } catch (Exception e) {
                    System.err.println("Error streaming alert recording from S3: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}
