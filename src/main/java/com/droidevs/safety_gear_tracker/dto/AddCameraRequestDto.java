package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record AddCameraRequestDto(
        @NotBlank(message = "Camera name cannot be blank")
        String name,
        @NotBlank(message = "IP address cannot be blank")
        @JsonProperty("ip_address")
        String ipAddress,
        @Positive(message = "Port must be a positive number")
        int port,
        @NotBlank(message = "Username cannot be blank")
        String username,
        @NotBlank(message = "Password cannot be blank")
        String password,
        @NotBlank(message = "RTSP URL cannot be blank")
        @JsonProperty("rtsp_url")
        String rtspUrl,
        @JsonProperty("required_safety_gear")
        Set<SafetyGearType> requiredSafetyGear,
        @NotNull(message = "Zone ID cannot be null")
        @JsonProperty("zone_id")
        Long zoneId
) {
}
