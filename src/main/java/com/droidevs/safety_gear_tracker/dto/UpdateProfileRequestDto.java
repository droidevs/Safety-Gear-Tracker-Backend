package com.droidevs.safety_gear_tracker.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequestDto(
        String firstname,
        String lastname,
        MultipartFile profilePicture
) {
}
