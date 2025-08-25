package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(@NotBlank(message = "OTP cannot be blank") @JsonProperty("otp") String otp) {
}
