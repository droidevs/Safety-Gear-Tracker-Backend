package com.droidevs.safety_gear_tracker.mapper;

import com.droidevs.safety_gear_tracker.auth.token.DailyCode;
import com.droidevs.safety_gear_tracker.auth.token.DailyCodeRepository;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final DailyCodeRepository dailyCodeRepository;

    public UserProfileDto toUserProfileDto(User user) {
        Optional<DailyCode> latestCode = dailyCodeRepository.findTopByUserOrderByCreatedAtDesc(user);
        return UserProfileDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .locked(user.isAccountNonLocked())
                .roles(user.getRoles())
                .zones(user.getZones())
                .currentDailyCode(latestCode.map(DailyCode::getCode).orElse("N/A"))
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    public UserSelfProfileDto toUserSelfProfileDto(User user) {
        return UserSelfProfileDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .locked(user.isAccountNonLocked())
                .roles(user.getRoles())
                .zones(user.getZones())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}
