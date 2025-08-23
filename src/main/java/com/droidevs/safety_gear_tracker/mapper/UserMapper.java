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
        return new UserProfileDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getRoles(),
                user.getZones(),
                latestCode.map(DailyCode::getCode).orElse("N/A"),
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
                user.getProfilePictureUrl()
        );
    }
}
