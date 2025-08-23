package com.droidevs.safety_gear_tracker.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
