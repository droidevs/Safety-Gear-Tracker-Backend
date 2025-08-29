package com.droidevs.safety_gear_tracker.auth.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class JwtSecretInfo {
    private String secretKey;
    private long expirationTime;
}
