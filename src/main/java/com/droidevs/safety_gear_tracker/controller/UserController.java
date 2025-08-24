
package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WeeklyCodeService weeklyCodeService;

    @PostMapping("/{email}/zones")
    public ResponseEntity<?> assignZonesToUser(@PathVariable String email, @RequestBody Set<Long> zoneIds) {
        userService.assignZonesToUser(email, zoneIds);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<UserPagingResponseDto> getAllUsers(UserPagingRequestDto request) {
        Page<UserProfileDto> page = userService.getAllUsers(request);
        return ResponseEntity.ok(new UserPagingResponseDto(page));
    }
    
    @GetMapping("/{email}")
    public ResponseEntity<UserProfileDto> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUser(email));
    }
    
    @GetMapping("/profile/me")
    public ResponseEntity<UserSelfProfileDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }
    
    @PutMapping("/profile/me")
    public ResponseEntity<UserSelfProfileDto> updateMyProfile(@ModelAttribute UpdateProfileRequestDto request) throws IOException {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
    
    @PutMapping("/{email}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable String email) {
        userService.deactivateUser(email);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{email}/activate")
    public ResponseEntity<?> activateUser(@PathVariable String email) {
        userService.activateUser(email);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{email}/zones/{zoneId}")
    public ResponseEntity<?> removeZoneFromUser(@PathVariable String email, @PathVariable Long zoneId) {
        userService.removeZoneFromUser(email, zoneId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{email}/latest-weekly-code")
    public ResponseEntity<Optional<WeeklyCode>> getLatestWeeklyCodeForUser(@PathVariable String email) {
        return ResponseEntity.ok(weeklyCodeService.getLatestWeeklyCodeForUser(email));
    }
    
    @PutMapping("/{email}/promote")
    public ResponseEntity<?> promoteToMaster(@PathVariable String email) {
        userService.promoteToMaster(email);
        return ResponseEntity.ok().build();
    }
}
