package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record AlertSummaryResponseDto(
    @JsonProperty("id")
    Long id,
    @JsonProperty("camera_name")
    String cameraName,
    @JsonProperty("description")
    String description,
    @JsonProperty("timestamp")
    LocalDateTime timestamp
) {
}
