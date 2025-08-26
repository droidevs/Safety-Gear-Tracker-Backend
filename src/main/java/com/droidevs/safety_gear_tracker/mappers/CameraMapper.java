package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraFullResponseDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.CameraSummaryDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;
import com.droidevs.safety_gear_tracker.model.Camera;

public interface CameraMapper {
    Camera toEntity(AddCameraRequestDto dto);
    CameraResponseDto toDto(Camera camera);
    CameraSummaryDto toSummaryDto(Camera camera);

    CameraFullResponseDto toFullDto(Camera camera);

    void updateCameraFromDto(UpdateCameraRequestDto dto, Camera camera);
}
