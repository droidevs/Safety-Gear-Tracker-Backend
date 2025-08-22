package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.mapper.UserMapper;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.RoleRepository;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;

    @Value("${master.user.email}")
    private String masterEmail;

    @PreAuthorize("hasRole('MASTER')")
    public void assignZonesToUser(String email, Set<Long> zoneIds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<Zone> zones = zoneIds.stream()
                .map(zoneId -> zoneRepository.findById(zoneId)
                        .orElseThrow(() -> new RuntimeException("Zone not found")))
                .collect(Collectors.toSet());

        user.setZones(zones);
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public UserProfileDto getUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMapper.toUserProfileDto(user);
    }
    
    public UserSelfProfileDto getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMapper.toUserSelfProfileDto(user);
    }
    
    public UserSelfProfileDto updateMyProfile(UpdateProfileRequestDto request) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getFirstname() != null && !request.getFirstname().isEmpty()) {
            user.setFirstname(request.getFirstname());
        }

        if (request.getLastname() != null && !request.getLastname().isEmpty()) {
            user.setLastname(request.getLastname());
        }

        MultipartFile profilePicture = request.getProfilePicture();
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePictureKey = "profile-pictures/" + user.getId() + "/" + UUID.randomUUID() + "-" + profilePicture.getOriginalFilename();
            s3Service.uploadFile(profilePictureKey, profilePicture.getInputStream());
            user.setProfilePictureUrl(profilePictureKey);
        }

        userRepository.save(user);
        return userMapper.toUserSelfProfileDto(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public void deactivateUser(String email) {
        if (email.equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLocked(true);
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLocked(false);
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public void deleteUser(String email) {
        if (email.equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public void removeZoneFromUser(String email, Long zoneId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Zone zone = zoneRepository.findById(zoneId).orElseThrow(() -> new RuntimeException("Zone not found"));
        user.getZones().remove(zone);
        userRepository.save(user);
    }
    
    @PreAuthorize("hasRole('MASTER')")
    public void promoteToMaster(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Role masterRole = roleRepository.findByName("MASTER").orElseThrow(() -> new RuntimeException("MASTER role not found"));
        user.getRoles().add(masterRole);
        userRepository.save(user);
    }
}
