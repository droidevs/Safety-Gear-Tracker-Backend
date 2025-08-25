package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ZoneSortCriteria {
    @JsonProperty("more_managers")
    MORE_MANAGERS,
    @JsonProperty("less_managers")
    LESS_MANAGERS,
    @JsonProperty("more_cameras")
    MORE_CAMERAS,
    @JsonProperty("less_cameras")
    LESS_CAMERAS,
    @JsonProperty("no_order")
    NO_ORDER
}
