
package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UserService {
    void assignZonesToUser(String email, Set<Long> zoneIds);
    List<UserProfileDto> getAllUsers();
    UserProfileDto getUser(String email);
    UserSelfProfileDto getMyProfile();
    UserSelfProfileDto updateMyProfile(UpdateProfileRequestDto request) throws IOException;
    void deactivateUser(String email);
    void activateUser(String email);
    void deleteUser(String email);
    void removeZoneFromUser(String email, Long zoneId);
    void promoteToMaster(String email);
}
