package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdateZoneRequestDto(
    @NotBlank(message = "Zone name cannot be blank")
    @JsonProperty("name")
    String name,
    @JsonProperty("description")
    String description
) {
}
