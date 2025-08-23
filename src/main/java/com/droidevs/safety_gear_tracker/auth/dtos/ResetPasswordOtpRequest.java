package com.droidevs.safety_gear_tracker.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordOtpRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "OTP is required")
        String otp,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword
) {
}
