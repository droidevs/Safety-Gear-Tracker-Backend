package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.CameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.mappers.CameraMapper;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CameraServiceImpl implements CameraService {

    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;
    private final VideoProcessingService videoProcessingService;
    private final RecordingService recordingService;
    private final CameraManagementService cameraManagementService;
    private final CameraMapper cameraMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CameraResponseDto> getAllCameras() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cameraRepository.findAll().stream()
                .filter(camera -> user.getZones().contains(camera.getZone()))
                .map(cameraMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CameraResponseDto> getCameraById(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cameraRepository.findById(id)
                .filter(camera -> user.getZones().contains(camera.getZone()))
                .map(cameraMapper::toDto);
    }

    @Override
    @Transactional
    public CameraResponseDto addCamera(CameraRequestDto cameraDto) {
        Camera camera = cameraMapper.toEntity(cameraDto);
        camera.setActive(true);
        Camera savedCamera = cameraRepository.save(camera);
        videoProcessingService.startProcessing(savedCamera);
        if (savedCamera.isRecordingActive()) {
            recordingService.startRecording(savedCamera);
        }
        return cameraMapper.toDto(savedCamera);
    }

    @Override
    @Transactional
    public Optional<CameraResponseDto> updateCamera(Long id, CameraRequestDto updatedCameraDto) {
        Optional<Camera> existingCameraOptional = getCameraByIdInternal(id);
        if (existingCameraOptional.isPresent()) {
            Camera cameraToUpdate = existingCameraOptional.get();

            if (!cameraToUpdate.getUsername().equals(updatedCameraDto.getUsername()) ||
                !cameraToUpdate.getPassword().equals(updatedCameraDto.getPassword())) {
                boolean credentialsChanged = cameraManagementService.changeCredentials(
                        cameraToUpdate.getIpAddress(), cameraToUpdate.getPort(),
                        cameraToUpdate.getUsername(), cameraToUpdate.getPassword(),
                        updatedCameraDto.getUsername(), updatedCameraDto.getPassword());

                if (!credentialsChanged) {
                    throw new RuntimeException("Failed to update camera credentials on the device.");
                }
            }

            cameraToUpdate.setName(updatedCameraDto.getName());
            cameraToUpdate.setIpAddress(updatedCameraDto.getIpAddress());
            cameraToUpdate.setPort(updatedCameraDto.getPort());
            cameraToUpdate.setUsername(updatedCameraDto.getUsername());
            cameraToUpdate.setPassword(updatedCameraDto.getPassword());
            cameraToUpdate.setRtspUrl(updatedCameraDto.getRtspUrl());
            cameraToUpdate.setRequiredSafetyGear(updatedCameraDto.getRequiredSafetyGear());

            if (updatedCameraDto.getZoneId() != null) {
                Zone zone = zoneRepository.findById(updatedCameraDto.getZoneId())
                        .orElseThrow(() -> new RuntimeException("Zone not found with id: " + updatedCameraDto.getZoneId()));
                cameraToUpdate.setZone(zone);
            } else {
                cameraToUpdate.setZone(null);
            }

            boolean wasActive = cameraToUpdate.isActive();
            cameraToUpdate.setActive(updatedCameraDto.isActive());
            if (wasActive && !cameraToUpdate.isActive()) {
                videoProcessingService.stopProcessing(cameraToUpdate.getId());
            } else if (!wasActive && cameraToUpdate.isActive()) {
                videoProcessingService.startProcessing(cameraToUpdate);
            }

            boolean wasRecordingActive = cameraToUpdate.isRecordingActive();
            cameraToUpdate.setRecordingActive(updatedCameraDto.isRecordingActive());
            if (wasRecordingActive && !cameraToUpdate.isRecordingActive()) {
                recordingService.stopRecording(cameraToUpdate.getId());
            } else if (!wasRecordingActive && cameraToUpdate.isRecordingActive()) {
                recordingService.startRecording(cameraToUpdate);
            }

            Camera updatedCameraEntity = cameraRepository.save(cameraToUpdate);
            return Optional.of(cameraMapper.toDto(updatedCameraEntity));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean deleteCamera(Long id) {
        Optional<Camera> cameraToDelete = getCameraByIdInternal(id);
        if (cameraToDelete.isPresent()) {
            videoProcessingService.stopProcessing(id);
            recordingService.stopRecording(id);
            cameraRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Optional<Camera> getCameraByIdInternal(Long id) {
        return cameraRepository.findById(id);
    }
}
