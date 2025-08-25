package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserResponsibilitySort {
    @JsonProperty("more_responsible")
    MORE_RESPONSIBLE,
    @JsonProperty("less_responsible")
    LESS_RESPONSIBLE
}
