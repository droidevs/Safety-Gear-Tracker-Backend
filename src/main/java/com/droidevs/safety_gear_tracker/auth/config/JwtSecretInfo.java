package com.droidevs.safety_gear_tracker.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class JwtSecretInfo {
    private String secretKey;
    private long expirationTime;
}
