package com.droidevs.safety_gear_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateZoneRequestDto(
    @NotBlank(message = "Zone name cannot be blank")
    String name,
    String description
) {
}
