package com.droidevs.safety_gear_tracker.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenRequest(
    @JsonProperty("refresh_token")
    String refreshToken
) {
}
