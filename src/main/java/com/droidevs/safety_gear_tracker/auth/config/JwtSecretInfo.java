package com.droidevs.safety_gear_tracker.auth.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtSecretInfo {

    private String secret_key;
    private Long expiration_time;
}
