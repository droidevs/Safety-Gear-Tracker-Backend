package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.CameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;

import java.util.List;
import java.util.Optional;

public interface CameraService {
    List<CameraResponseDto> getAllCameras();
    Optional<CameraResponseDto> getCameraById(Long id);
    CameraResponseDto addCamera(CameraRequestDto cameraDto);
    Optional<CameraResponseDto> updateCamera(Long id, CameraRequestDto updatedCameraDto);
    boolean deleteCamera(Long id);
}
