package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;

import java.util.List;

public record SafetyViolation(
        BoundingBox personBoundingBox,
        List<SafetyGearType> missingGear
) {
}
