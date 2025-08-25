package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.PagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;

import java.util.Optional;

public interface CameraService {
    PagingResponseDto<CameraResponseDto> getAllCameras(CameraPagingRequestDto request);
    Optional<CameraResponseDto> getCameraById(Long id);
    CameraResponseDto addCamera(AddCameraRequestDto cameraDto);
    Optional<CameraResponseDto> updateCamera(Long id, UpdateCameraRequestDto updatedCameraDto);
    void deleteCamera(Long id);
}
