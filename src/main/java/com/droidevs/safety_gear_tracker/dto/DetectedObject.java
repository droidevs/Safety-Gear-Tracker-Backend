package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetectedObject(
    @JsonProperty("label")
    String label,
    @JsonProperty("bounding_box")
    BoundingBox boundingBox
) {
}
