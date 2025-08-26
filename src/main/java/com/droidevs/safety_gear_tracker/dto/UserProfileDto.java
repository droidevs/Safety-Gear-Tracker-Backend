package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record UserProfileDto(
        @JsonProperty("id")
        Long id,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("lastname")
        String lastname,
        @JsonProperty("email")
        String email,
        @JsonProperty("enabled")
        boolean enabled,
        @JsonProperty("active")
        boolean active,
        @JsonProperty("roles")
        Set<Role> roles,
        @JsonProperty("zones")
        Set<Zone> zones,
        @JsonProperty("current_weekly_code")
        String currentWeeklyCode,
        @JsonProperty("profile_picture_url")
        String profilePictureUrl
) {
}
