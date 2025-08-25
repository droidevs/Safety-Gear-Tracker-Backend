package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record AlertResponseDto(
        @JsonProperty("id")
        Long id,
        @JsonProperty("camera_name")
        String cameraName,
        @JsonProperty("description")
        String description,
        @JsonProperty("timestamp")
        LocalDateTime timestamp,
        @JsonProperty("screenshot_url")
        String screenshotUrl,
        @JsonProperty("recording_id")
        Long recordingId
) {
}
