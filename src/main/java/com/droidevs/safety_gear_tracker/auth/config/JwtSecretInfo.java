package com.droidevs.safety_gear_tracker.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * BUG-08 FIX: Original class had plain fields with no @Value bindings.
 * getSecretKey() returned null → Keys.hmacShaKeyFor(null) → NPE.
 * Solution: bind both fields from application.properties via @Value.
 */
@Getter
@Component
public class JwtSecretInfo {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long expirationTime;
}