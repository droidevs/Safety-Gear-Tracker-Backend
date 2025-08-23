package com.droidevs.safety_gear_tracker.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(@NotBlank(message = "OTP cannot be blank") String otp) {
}
