package com.droidevs.safety_gear_tracker.auth.dtos;

public record AuthenticationResponse(String token, long expiresIn) {
}
