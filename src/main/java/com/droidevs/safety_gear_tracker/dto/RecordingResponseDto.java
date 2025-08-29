package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record RecordingResponseDto(
    @JsonProperty("id")
    Long id,
    @JsonProperty("camera_name")
    String cameraName,
    @JsonProperty("video_url")
    String videoUrl,
    @JsonProperty("timestamp")
    LocalDateTime timestamp
) {
}
