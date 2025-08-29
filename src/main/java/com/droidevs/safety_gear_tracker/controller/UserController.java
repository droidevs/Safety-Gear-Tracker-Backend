
package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final WeeklyCodeService weeklyCodeService;

    @PostMapping("/{id}/zones")
    public ResponseEntity<?> assignZonesToUser(@PathVariable("id") Long id, @RequestBody Set<Long> zoneIds) {
        userService.assignZonesToUser(id, zoneIds);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<UserSummaryPagingResponseDto> getAllUsers(UserPagingRequestDto request) {
        UserSummaryPagingResponseDto response = userService.getAllUsers(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @GetMapping("/profile/me")
    public ResponseEntity<UserSelfProfileDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }
    
    @PutMapping("/profile/me")
    public ResponseEntity<UserSelfProfileDto> updateMyProfile(@ModelAttribute UpdateProfileRequestDto request) throws IOException {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable("id") Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable("id") Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}/zones/{zoneId}")
    public ResponseEntity<?> removeZoneFromUser(@PathVariable("id") Long id, @PathVariable("zoneId") Long zoneId) {
        userService.removeZoneFromUser(id, zoneId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/latest-weekly-code")
    public ResponseEntity<Optional<WeeklyCode>> getLatestWeeklyCodeForUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(weeklyCodeService.getLatestWeeklyCodeForUser(id));
    }
    
    @PutMapping("/{id}/promote")
    public ResponseEntity<?> promoteToMaster(@PathVariable("id") Long id) {
        userService.promoteToMaster(id);
        return ResponseEntity.ok().build();
    }
}
