package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ZoneDetailResponseDto(
    @JsonProperty("id")
    Long id,
    @JsonProperty("name")
    String name,
    @JsonProperty("description")
    String description,
    @JsonProperty("user_count")
    long userCount
) {
}
