package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public record AlertRequestDto(
        @NotNull(message = "Camera ID cannot be null")
        @JsonProperty("camera_id")
        Long cameraId,
        @JsonProperty("description")
        String description,
        @JsonProperty("screenshot")
        MultipartFile screenshot
) {
}
