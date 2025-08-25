package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoundingBox(
    @JsonProperty("x")
    int x,
    @JsonProperty("y")
    int y,
    @JsonProperty("width")
    int width,
    @JsonProperty("height")
    int height
) {
}
