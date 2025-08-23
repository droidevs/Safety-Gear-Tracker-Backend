package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;

import java.util.List;

public record CameraRequestDto(
        String name,
        String ipAddress,
        int port,
        String username,
        String password,
        String rtspUrl,
        boolean active,
        boolean isRecordingActive,
        Long zoneId,
        List<SafetyGearType> requiredSafetyGear
) {
}
