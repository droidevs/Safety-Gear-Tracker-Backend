package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;

import java.util.List;

public record CameraResponseDto(
        Long id,
        String name,
        String ipAddress,
        int port,
        String rtspUrl,
        boolean active,
        boolean isRecordingActive,
        Long zoneId,
        List<SafetyGearType> requiredSafetyGear,
        String username
) {
}
