package com.droidevs.safety_gear_tracker.dto;

import org.springframework.web.multipart.MultipartFile;

public record AlertRequestDto(
        String cameraName,
        String description,
        MultipartFile screenshot
) {
}
