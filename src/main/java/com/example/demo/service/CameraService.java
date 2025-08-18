package com.example.demo.service;

import com.example.demo.dto.CameraRequestDto;
import com.example.demo.dto.CameraResponseDto;
import com.example.demo.model.Camera;
import com.example.demo.repository.CameraRepository; // Import the repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CameraService {

 private final CameraRepository cameraRepository; // Inject the repository

    private final VideoProcessingService videoProcessingService;

    @Autowired
    public CameraService(CameraRepository cameraRepository, VideoProcessingService videoProcessingService) {
        this.cameraRepository = cameraRepository;
        this.videoProcessingService = videoProcessingService;
    }   

 @Transactional(readOnly = true) // Read-only transaction
    public List<CameraResponseDto> getAllCameras() {
 return cameraRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

 @Transactional(readOnly = true) // Read-only transaction
    public Optional<CameraResponseDto> getCameraById(String id) {
 return cameraRepository.findById(id)
                .map(this::convertToDto);
    }

 @Transactional // Transactional for write operations
    public CameraResponseDto addCamera(CameraRequestDto cameraDto) {
 Camera camera = convertToEntity(cameraDto);
 camera.setActive(true); // Set camera to active by default
 Camera savedCamera = cameraRepository.save(camera); // Save to database
 videoProcessingService.startProcessing(savedCamera); // Start processing after saving
 return convertToDto(savedCamera);
    }
    public Optional<CameraResponseDto> updateCamera(String id, CameraRequestDto updatedCameraDto) {
        Optional<Camera> existingCamera = getCameraByIdInternal(id); // Internal helper to get entity
        if (existingCamera.isPresent()) {
            Camera cameraToUpdate = existingCamera.get();
            cameraToUpdate.setName(updatedCameraDto.getName());
            cameraToUpdate.setIpAddress(updatedCameraDto.getIpAddress());
            cameraToUpdate.setPort(updatedCameraDto.getPort());
            cameraToUpdate.setUsername(updatedCameraDto.getUsername());
            cameraToUpdate.setPassword(updatedCameraDto.getPassword());

            // Check if active status changed
            boolean wasActive = cameraToUpdate.isActive();
            cameraToUpdate.setActive(updatedCameraDto.isActive()); // Assuming active status is in DTO

            if (wasActive && !cameraToUpdate.isActive()) {
                videoProcessingService.stopProcessing(cameraToUpdate.getId()); // Stop if deactivated
            } else if (!wasActive && cameraToUpdate.isActive()) {
                videoProcessingService.startProcessing(cameraToUpdate); // Start if activated
            }
 Camera updatedCameraEntity = cameraRepository.save(cameraToUpdate); // Save updated entity
 return Optional.of(convertToDto(updatedCameraEntity));
 } else {
            return Optional.empty();
        }
    }

    // Internal helper method to get Camera entity
    private Optional<Camera> getCameraByIdInternal(String id) {
 Optional<Camera> existingCamera = cameraRepository.findById(id);
 return existingCamera;
    }

 // Helper method to convert Camera entity to CameraResponseDto
 private CameraResponseDto convertToDto(Camera camera) {
 return new CameraResponseDto(
                camera.getId(),
                camera.getName(),
                camera.getIpAddress(),
                camera.getPort()
 , camera.isActive(), // Include active status in DTO
 camera.getRequiredSafetyGear() // Include required safety gear
        );
    }

 // Helper method to convert CameraRequestDto to Camera entity
    private Camera convertToEntity(CameraRequestDto cameraDto) {
        Camera camera = new Camera();
        camera.setName(cameraDto.getName());
        camera.setIpAddress(cameraDto.getIpAddress());
        camera.setPort(cameraDto.getPort());
        camera.setActive(cameraDto.isActive()); // Set active status from DTO
        camera.setRequiredSafetyGear(cameraDto.getRequiredSafetyGear()); // Set required safety gear
        return camera;
    }


    public boolean deleteCamera(String id) {
        Optional<Camera> cameraToDelete = getCameraByIdInternal(id);
        if (cameraToDelete.isPresent()) {
 videoProcessingService.stopProcessing(id); // Stop processing before deleting            cameraRepository.deleteById(id);
            cameraRepository.deleteById(id);
 return true;
        }
        return false;
    }
}