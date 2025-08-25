package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SafetyViolation(
        @JsonProperty("person_bounding_box")
        BoundingBox personBoundingBox,
        @JsonProperty("missing_gear")
        List<SafetyGearType> missingGear
) {
}
