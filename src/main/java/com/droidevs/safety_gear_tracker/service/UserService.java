
package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSummaryPagingResponseDto;

import java.io.IOException;
import java.util.Set;

public interface UserService {
    void assignZonesToUser(Long id, Set<Long> zoneIds);
    UserSummaryPagingResponseDto getAllUsers(UserPagingRequestDto request);
    UserProfileDto getUser(Long id);
    UserSelfProfileDto getMyProfile();
    UserSelfProfileDto updateMyProfile(UpdateProfileRequestDto request) throws IOException;
    void deactivateUser(Long id);
    void activateUser(Long id);
    void deleteUser(Long id);
    void removeZoneFromUser(Long id, Long zoneId);
    void promoteToMaster(Long id);
}
