package com.droidevs.safety_gear_tracker.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record DailyCodeValidationRequest(@NotBlank(message = "Code cannot be blank") String code) {
}
