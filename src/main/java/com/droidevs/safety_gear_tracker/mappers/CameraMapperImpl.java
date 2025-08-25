package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraFullResponseDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;
import com.droidevs.safety_gear_tracker.model.Camera;
import org.springframework.stereotype.Component;

@Component
public class CameraMapperImpl implements CameraMapper {
    @Override
    public Camera toEntity(AddCameraRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Camera camera = new Camera();
        camera.setName(dto.name());
        camera.setIpAddress(dto.ipAddress());
        camera.setPort(dto.port());
        camera.setUsername(dto.username());
        camera.setPassword(dto.password());
        camera.setRtspUrl(dto.rtspUrl());
        camera.setRequiredSafetyGear(dto.requiredSafetyGear());
        return camera;
    }

    @Override
    public CameraResponseDto toDto(Camera camera) {
        if (camera == null) {
            return null;
        }
        return new CameraResponseDto(
                camera.getId(),
                camera.getName(),
                camera.getIpAddress(),
                camera.getPort(),
                camera.isActive(),
                camera.isRecordingActive(),
                camera.getZone() != null ? camera.getZone().getId() : null,
                camera.getRequiredSafetyGear().stream().toList()
        );
    }

    @Override
    public CameraFullResponseDto toFullDto(Camera camera) {
        if (camera == null) {
            return null;
        }
        return new CameraFullResponseDto(
                camera.getId(),
                camera.getName(),
                camera.getIpAddress(),
                camera.getPort(),
                camera.getUsername(),
                camera.getPassword(),
                camera.isActive(),
                camera.isRecordingActive(),
                camera.getZone() != null ? camera.getZone().getId() : null,
                camera.getRequiredSafetyGear().stream().toList()
        );
    }

    @Override
    public void updateCameraFromDto(UpdateCameraRequestDto dto, Camera camera) {
        if (dto == null || camera == null) {
            return;
        }
        camera.setName(dto.name());
        camera.setIpAddress(dto.ipAddress());
        camera.setPort(dto.port());
        camera.setUsername(dto.username());
        camera.setPassword(dto.password());
        camera.setRtspUrl(dto.rtspUrl());
        camera.setRequiredSafetyGear(dto.requiredSafetyGear());
        camera.setActive(dto.active());
        camera.setRecordingActive(dto.isRecordingActive());
    }
}
