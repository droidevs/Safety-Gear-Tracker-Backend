package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponse(
    @JsonProperty("token")
    String token,
    @JsonProperty("expires_in")
    long expiresIn
) {
}
