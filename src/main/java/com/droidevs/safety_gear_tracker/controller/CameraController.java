package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.AddCameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.dto.CameraSummaryPagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.UpdateCameraRequestDto;
import com.droidevs.safety_gear_tracker.service.CameraService;
import com.droidevs.safety_gear_tracker.service.VideoProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Optional;

@RestController
@RequestMapping("/api/cameras")
@RequiredArgsConstructor
public class CameraController {

    private final CameraService cameraService;
    private final VideoProcessingService videoProcessingService;

    @GetMapping
    public ResponseEntity<CameraSummaryPagingResponseDto> getAllCameras(CameraPagingRequestDto request) {
        CameraSummaryPagingResponseDto cameras = cameraService.getAllCameras(request);
        return new ResponseEntity<>(cameras, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCameraById(@PathVariable Long id) {
        Optional<?> camera = cameraService.getCameraById(id);
        return camera.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<CameraResponseDto> addCamera(@Valid @RequestBody AddCameraRequestDto cameraDto) {
        CameraResponseDto newCamera = cameraService.addCamera(cameraDto);
        return new ResponseEntity<>(newCamera, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraResponseDto> updateCamera(@PathVariable Long id, @Valid @RequestBody UpdateCameraRequestDto camera) {
        Optional<CameraResponseDto> updatedCamera = cameraService.updateCamera(id, camera);
        return updatedCamera.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable Long id) {
        cameraService.deleteCamera(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{id}/feed", produces = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<byte[]> getCameraFeed(@PathVariable Long id) {
        return videoProcessingService.getCameraFeed(id);
    }
}
