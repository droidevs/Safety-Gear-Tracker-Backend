package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileDto(
        @JsonProperty("id")
        Long id,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("lastname")
        String lastname,
        @JsonProperty("email")
        String email,
        @JsonProperty("otp_verified")
        boolean otpVerified, // Replaced 'enabled' with 'otpVerified'
        @JsonProperty("is_active_by_master")
        boolean isActiveByMaster, // Added 'isActiveByMaster'
        @JsonProperty("active")
        boolean active,
        @JsonProperty("current_weekly_code")
        WeeklyCode currentWeeklyCode,
        @JsonProperty("profile_picture_url")
        String profilePictureUrl
) {
}
