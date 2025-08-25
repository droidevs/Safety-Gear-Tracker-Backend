package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.PagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;
import com.droidevs.safety_gear_tracker.mappers.CameraMapper;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public PagingResponseDto<CameraResponseDto> getAllCameras(CameraPagingRequestDto request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Specification<Camera> spec = Specification.where((root, query, cb) -> root.get("zone").in(user.getZones()));

        if (request.getZone() != null && !request.getZone().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("zone").get("name"), request.getZone()));
        }

        if (request.getActivationStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), request.getActivationStatus() == com.droidevs.safety_gear_tracker.dto.ActivationStatus.ACTIVE));
        }

        if (request.getManagementStatus() != null) {
            // This is a placeholder for more complex logic.
            // For now, we'll just sort by the number of users in the zone.
            if (request.getManagementStatus() == com.droidevs.safety_gear_tracker.dto.ManagementStatus.MORE_MANAGED) {
                // Order by user count descending
            } else {
                // Order by user count ascending
            }
        }

        Page<Camera> cameraPage = cameraRepository.findAll(spec, request.toPageable());
        return new PagingResponseDto<>(cameraPage.map(cameraMapper::toDto));
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
    @PreAuthorize("hasRole('MASTER')")
    public CameraResponseDto addCamera(AddCameraRequestDto cameraDto) {
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
    public Optional<CameraResponseDto> updateCamera(Long id, UpdateCameraRequestDto updatedCameraDto) {
        Optional<Camera> existingCameraOptional = getCameraByIdInternal(id);
        if (existingCameraOptional.isPresent()) {
            Camera cameraToUpdate = existingCameraOptional.get();

            if (!cameraToUpdate.getUsername().equals(updatedCameraDto.username()) ||
                !cameraToUpdate.getPassword().equals(updatedCameraDto.password())) {
                boolean credentialsChanged = cameraManagementService.changeCredentials(
                        cameraToUpdate.getIpAddress(), cameraToUpdate.getPort(),
                        cameraToUpdate.getUsername(), cameraToUpdate.getPassword(),
                        updatedCameraDto.username(), updatedCameraDto.password());

                if (!credentialsChanged) {
                    throw new RuntimeException("Failed to update camera credentials on the device.");
                }
            }

            cameraMapper.updateCameraFromDto(updatedCameraDto, cameraToUpdate);


            if (updatedCameraDto.zoneId() != null) {
                Zone zone = zoneRepository.findById(updatedCameraDto.zoneId())
                        .orElseThrow(() -> new RuntimeException("Zone not found with id: " + updatedCameraDto.zoneId()));
                cameraToUpdate.setZone(zone);
            } else {
                cameraToUpdate.setZone(null);
            }

            boolean wasActive = cameraToUpdate.isActive();
            cameraToUpdate.setActive(updatedCameraDto.active());
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
    @PreAuthorize("hasRole('MASTER')")
    public void deleteCamera(Long id) {
        Optional<Camera> cameraToDelete = getCameraByIdInternal(id);
        if (cameraToDelete.isPresent()) {
            videoProcessingService.stopProcessing(id);
            recordingService.stopRecording(id);
            cameraRepository.deleteById(id);
        }
    }

    private Optional<Camera> getCameraByIdInternal(Long id) {
        return cameraRepository.findById(id);
    }
}
