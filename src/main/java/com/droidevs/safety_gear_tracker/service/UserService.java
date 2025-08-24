
package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.Set;

public interface UserService {
    void assignZonesToUser(String email, Set<Long> zoneIds);
    Page<UserProfileDto> getAllUsers(UserPagingRequestDto request);
    UserProfileDto getUser(String email);
    UserSelfProfileDto getMyProfile();
    UserSelfProfileDto updateMyProfile(UpdateProfileRequestDto request) throws IOException;
    void deactivateUser(String email);
    void activateUser(String email);
    void deleteUser(String email);
    void removeZoneFromUser(String email, Long zoneId);
    void promoteToMaster(String email);
}
