package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ZoneSummaryResponseDto(
    @JsonProperty("id")
    Long id,
    @JsonProperty("name")
    String name
) {
}
