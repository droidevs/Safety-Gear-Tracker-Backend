package com.droidevs.safety_gear_tracker.dto;

import java.time.LocalDateTime;

public record AlertResponseDto(
        Long id,
        String cameraName,
        String description,
        LocalDateTime timestamp,
        String screenshotUrl,
        Long recordingId
) {
}
