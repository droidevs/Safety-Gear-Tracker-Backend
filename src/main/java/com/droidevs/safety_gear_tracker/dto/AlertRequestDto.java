package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

public record AlertRequestDto(
        @JsonProperty("camera_name")
        String cameraName,
        @JsonProperty("description")
        String description,
        @JsonProperty("screenshot")
        MultipartFile screenshot
) {
}
