package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.Zone;

import java.util.Set;

public record UserSelfProfileDto(
        Long id,
        String firstname,
        String lastname,
        String email,
        boolean enabled,
        boolean locked,
        Set<Role> roles,
        Set<Zone> zones,
        String profilePictureUrl
) {
}
