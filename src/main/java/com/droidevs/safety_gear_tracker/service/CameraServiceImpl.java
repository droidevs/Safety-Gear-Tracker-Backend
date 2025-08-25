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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        Specification<Camera> spec = (root, query, cb) -> {
            if (user.getRoles().stream().anyMatch(role -> role.getName().equals("MASTER"))) {
                return cb.conjunction();
            } else {
                return root.get("zone").in(user.getZones());
            }
        };

        if (request.getZone() != null && !request.getZone().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("zone").get("name"), request.getZone()));
        }

        if (request.getActivationStatus() != null) {
            boolean isActive = request.getActivationStatus() == com.droidevs.safety_gear_tracker.dto.ActivationStatus.ACTIVE;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), isActive));
        }

        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        Page<Camera> cameraPage;

        if (request.getManagementStatus() != null) {
            if (request.getManagementStatus() == com.droidevs.safety_gear_tracker.dto.ManagementStatus.MORE_MANAGED) {
                cameraPage = cameraRepository.findAllOrderByUserCountDesc(spec, pageable);
            } else {
                cameraPage = cameraRepository.findAllOrderByUserCountAsc(spec, pageable);
            }
        } else {
            cameraPage = cameraRepository.findAll(spec, pageable);
        }

        return new PagingResponseDto<>(cameraPage.map(cameraMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CameraResponseDto> getCameraById(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Camera> cameraOptional = cameraRepository.findById(id);

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("MASTER"))) {
            return cameraOptional.map(cameraMapper::toDto);
        }

        return cameraOptional
                .filter(camera -> user.getZones().contains(camera.getZone()))
                .map(cameraMapper::toDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public CameraResponseDto addCamera(AddCameraRequestDto cameraDto) {
        Camera camera = cameraMapper.toEntity(cameraDto);

        Zone zone = zoneRepository.findById(cameraDto.zoneId())
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + cameraDto.zoneId()));
        camera.setZone(zone);
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
    @PreAuthorize("hasRole('MASTER')")
    public Optional<CameraResponseDto> updateCamera(Long id, UpdateCameraRequestDto updatedCameraDto) {
        return cameraRepository.findById(id).map(cameraToUpdate -> {
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

            Zone zone = zoneRepository.findById(updatedCameraDto.zoneId())
                    .orElseThrow(() -> new RuntimeException("Zone not found with id: " + updatedCameraDto.zoneId()));
            cameraToUpdate.setZone(zone);

            boolean wasActive = cameraToUpdate.isActive();
            if (wasActive && !updatedCameraDto.active()) {
                videoProcessingService.stopProcessing(id);
            } else if (!wasActive && updatedCameraDto.active()) {
                videoProcessingService.startProcessing(cameraToUpdate);
            }
            cameraToUpdate.setActive(updatedCameraDto.active());

            boolean wasRecordingActive = cameraToUpdate.isRecordingActive();
            if (wasRecordingActive && !updatedCameraDto.isRecordingActive()) {
                recordingService.stopRecording(id);
            } else if (!wasRecordingActive && updatedCameraDto.isRecordingActive()) {
                recordingService.startRecording(cameraToUpdate);
            }
            cameraToUpdate.setRecordingActive(updatedCameraDto.isRecordingActive());

            Camera updatedCameraEntity = cameraRepository.save(cameraToUpdate);
            return cameraMapper.toDto(updatedCameraEntity);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MASTER')")
    public void deleteCamera(Long id) {
        if (cameraRepository.existsById(id)) {
            videoProcessingService.stopProcessing(id);
            recordingService.stopRecording(id);
            cameraRepository.deleteById(id);
        }
    }
}
