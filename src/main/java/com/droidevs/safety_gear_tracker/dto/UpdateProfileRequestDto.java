package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequestDto(
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("lastname")
        String lastname,
        @JsonProperty("profile_picture")
        MultipartFile profilePicture
) {
}
