package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record CameraFullResponseDto(
        @JsonProperty("id")
        Long id,
        @JsonProperty("name")
        String name,
        @JsonProperty("ip_address")
        String ipAddress,
        @JsonProperty("port")
        int port,

        @JsonProperty("username")
        String username,

        @JsonProperty("password")
        String password,
        @JsonProperty("active")
        boolean active,
        @JsonProperty("is_recording_active")
        boolean isRecordingActive,
        @JsonProperty("zone_id")
        Long zoneId,
        @JsonProperty("required_safety_gear")
        List<SafetyGearType> requiredSafetyGear
) {
}
