package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;
import com.droidevs.safety_gear_tracker.model.Camera;

public interface CameraMapper {
    Camera toEntity(AddCameraRequestDto dto);
    CameraResponseDto toDto(Camera camera);
    void updateCameraFromDto(UpdateCameraRequestDto dto, Camera camera);
}
