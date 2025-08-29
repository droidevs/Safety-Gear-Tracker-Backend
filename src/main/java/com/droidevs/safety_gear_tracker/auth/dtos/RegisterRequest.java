package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @JsonProperty("firstname")
        String firstname,

        @NotBlank(message = "Last name is required")
        @JsonProperty("lastname")
        String lastname,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @JsonProperty("password")
        String password
) {
}
