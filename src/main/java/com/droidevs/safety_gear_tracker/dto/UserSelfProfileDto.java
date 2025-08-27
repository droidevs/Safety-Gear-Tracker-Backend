package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSelfProfileDto(
        @JsonProperty("id")
        Long id,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("lastname")
        String lastname,
        @JsonProperty("email")
        String email,
        @JsonProperty("profile_picture_url")
        String profilePictureUrl,
        @JsonProperty("active")
        boolean active,
        @JsonProperty("weekly_code_validated")
        boolean weeklyCodeVerified,
        @JsonProperty("otp_validated")
        boolean otpVerified,
        @JsonProperty("is_active_by_master")
        boolean isActiveByMaster // Renamed from activatedByMaster to isActiveByMaster to align with User model
) {
}
