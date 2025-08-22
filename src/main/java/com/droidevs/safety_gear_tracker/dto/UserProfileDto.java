package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.Zone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private boolean enabled;
    private boolean locked;
    private Set<Role> roles;
    private Set<Zone> zones;
    private String currentDailyCode;
    private String profilePictureUrl;
}
