package com.droidevs.safety_gear_tracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private final String issuerUri;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getAuthConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("issuerUri", issuerUri);
        config.put("tokenEndpoint", issuerUri + "/protocol/openid-connect/token");
        // You can add other relevant Keycloak endpoints here if needed
        return ResponseEntity.ok(config);
    }
}
