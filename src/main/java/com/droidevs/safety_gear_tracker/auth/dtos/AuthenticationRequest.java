package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AuthenticationRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @JsonProperty("password")
        String password
) {
}
