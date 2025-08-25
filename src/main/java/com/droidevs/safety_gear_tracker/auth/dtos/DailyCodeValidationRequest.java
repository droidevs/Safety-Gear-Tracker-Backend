package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record DailyCodeValidationRequest(@NotBlank(message = "Code cannot be blank") @JsonProperty("code") String code) {
}
