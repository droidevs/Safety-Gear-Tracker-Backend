package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.CameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import org.springframework.stereotype.Component;

@Component
public class CameraMapper {

    private final ZoneRepository zoneRepository;

    public CameraMapper(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public CameraResponseDto toDto(Camera camera) {
        return new CameraResponseDto(
                camera.getId(),
                camera.getName(),
                camera.getIpAddress(),
                camera.getPort(),
                camera.getRtspUrl(),
                camera.isActive(),
                camera.isRecordingActive(),
                camera.getZone() != null ? camera.getZone().getId() : null,
                camera.getRequiredSafetyGear(),
                camera.getUsername()
        );
    }

    public Camera toEntity(CameraRequestDto cameraDto) {
        Camera camera = new Camera();
        camera.setName(cameraDto.name());
        camera.setIpAddress(cameraDto.ipAddress());
        camera.setPort(cameraDto.port());
        camera.setUsername(cameraDto.username());
        camera.setPassword(cameraDto.password());
        camera.setRtspUrl(cameraDto.rtspUrl());
        camera.setActive(cameraDto.active());
        camera.setRecordingActive(cameraDto.isRecordingActive());
        camera.setRequiredSafetyGear(cameraDto.requiredSafetyGear());

        if (cameraDto.zoneId() != null) {
            Zone zone = zoneRepository.findById(cameraDto.zoneId())
                    .orElseThrow(() -> new RuntimeException("Zone not found with id: " + cameraDto.zoneId()));
            camera.setZone(zone);
        }

        return camera;
    }
}
