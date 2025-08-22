package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.CameraRequestDto;
import com.droidevs.safety_gear_tracker.dto.CameraResponseDto;
import com.droidevs.safety_gear_tracker.service.CameraService;
import com.droidevs.safety_gear_tracker.service.VideoProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraService cameraService;
    private final VideoProcessingService videoProcessingService;

    @Autowired
    public CameraController(CameraService cameraService,
                            VideoProcessingService videoProcessingService) {
        this.cameraService = cameraService;
        this.videoProcessingService = videoProcessingService;
    }

    @GetMapping
    public ResponseEntity<List<CameraResponseDto>> getAllCameras() {
        List<CameraResponseDto> cameras = cameraService.getAllCameras();
        return new ResponseEntity<>(cameras, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraResponseDto> getCameraById(@PathVariable Long id) {
        Optional<CameraResponseDto> camera = cameraService.getCameraById(id);
        return camera.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<CameraResponseDto> addCamera(@RequestBody CameraRequestDto cameraDto) {
        CameraResponseDto newCamera = cameraService.addCamera(cameraDto);
        return new ResponseEntity<>(newCamera, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraResponseDto> updateCamera(@PathVariable Long id, @RequestBody CameraRequestDto camera) {
        Optional<CameraResponseDto> updatedCamera = cameraService.updateCamera(id, camera);
        return updatedCamera.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable Long id) {
        boolean deleted = cameraService.deleteCamera(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{id}/feed", produces = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<byte[]> getCameraFeed(@PathVariable Long id) {
        return videoProcessingService.getCameraFeed(id);
    }
}
