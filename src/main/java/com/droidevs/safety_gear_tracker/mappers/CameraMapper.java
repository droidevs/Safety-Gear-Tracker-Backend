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
        camera.setName(cameraDto.getName());
        camera.setIpAddress(cameraDto.getIpAddress());
        camera.setPort(cameraDto.getPort());
        camera.setUsername(cameraDto.getUsername());
        camera.setPassword(cameraDto.getPassword());
        camera.setRtspUrl(cameraDto.getRtspUrl());
        camera.setActive(cameraDto.isActive());
        camera.setRecordingActive(cameraDto.isRecordingActive());
        camera.setRequiredSafetyGear(cameraDto.getRequiredSafetyGear());

        if (cameraDto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(cameraDto.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Zone not found with id: " + cameraDto.getZoneId()));
            camera.setZone(zone);
        }

        return camera;
    }
}
