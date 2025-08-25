package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordOtpRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "OTP is required")
        @JsonProperty("otp")
        String otp,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @JsonProperty("new_password")
        String newPassword
) {
}
