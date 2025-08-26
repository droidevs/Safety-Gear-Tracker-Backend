package com.droidevs.safety_gear_tracker.mapper;

import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.droidevs.safety_gear_tracker.auth.token.WeeklyCodeRepository;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSummaryResponseDto;
import com.droidevs.safety_gear_tracker.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final WeeklyCodeRepository weeklyCodeRepository;

    public UserProfileDto toUserProfileDto(User user) {
        Optional<WeeklyCode> latestCode = weeklyCodeRepository.findTopByUserOrderByCreatedAtDesc(user);
        return new UserProfileDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getRoles(),
                user.getZones(),
                latestCode.map(WeeklyCode::getCode).orElse("N/A"),
                user.getProfilePictureUrl()
        );
    }

    public UserSelfProfileDto toUserSelfProfileDto(User user) {
        return new UserSelfProfileDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getRoles(),
                user.getZones(),
                user.getProfilePictureUrl(),
                user.isActive()
        );
    }

    public UserSummaryResponseDto toUserSummaryResponseDto(User user) {
        return new UserSummaryResponseDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getProfilePictureUrl()
        );
    }
}
