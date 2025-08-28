package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSummaryPagingResponseDto;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.handler.exception.S3OperationException;
import com.droidevs.safety_gear_tracker.handler.exception.SelfDeactivationException;
import com.droidevs.safety_gear_tracker.handler.exception.SelfDeletionException;
import com.droidevs.safety_gear_tracker.handler.exception.UserNotFoundException;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.RoleRepository;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import com.droidevs.safety_gear_tracker.repository.UserSpecification;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import com.droidevs.safety_gear_tracker.mapper.UserMapper; // Added this import
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;

    @Value("${master.user.email}")
    private String masterEmail;

    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void assignZonesToUser(Long id, Set<Long> zoneIds) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        Set<Zone> zones = zoneIds.stream()
                .map(zoneId -> zoneRepository.findById(zoneId)
                        .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId)))
                .collect(Collectors.toSet());

        user.setZones(zones);
        user.updateZoneCount();
        userRepository.save(user);
    }
    
    @Override
    public UserSummaryPagingResponseDto getAllUsers(UserPagingRequestDto request) {
        Pageable pageable = request.toPageable();
        Specification<User> spec = Specification.where(null);

        if (request.getZoneId() != null) {
            spec = spec.and(UserSpecification.hasZone(request.getZoneId()));
        }

        if (request.getActive() != null) {
            spec = spec.and(UserSpecification.isActive(request.getActive()));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return new UserSummaryPagingResponseDto(userPage.map(userMapper::toUserSummaryResponseDto));
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public UserProfileDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return userMapper.toUserProfileDto(user);
    }
    
    @Override
    public UserSelfProfileDto getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toUserSelfProfileDto(user);
    }
    
    @Override
    public UserSelfProfileDto updateMyProfile(UpdateProfileRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (request.firstname() != null && !request.firstname().isEmpty()) {
            user.setFirstname(request.firstname());
        }

        if (request.lastname() != null && !request.lastname().isEmpty()) {
            user.setLastname(request.lastname());
        }

        MultipartFile profilePicture = request.profilePicture();
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePictureKey = "profile-pictures/" + user.getId() + "/" + UUID.randomUUID() + "-" + profilePicture.getOriginalFilename();
            try {
                s3Service.uploadFile(profilePictureKey, profilePicture.getInputStream());
            } catch (IOException e) { // IOException from profilePicture.getInputStream()
                throw new S3OperationException("Failed to read profile picture data for upload to S3 for user: " + user.getId(), e);
            } catch (S3OperationException e) { // S3OperationException from s3Service.uploadFile
                throw new S3OperationException("Failed to upload profile picture to S3 for user: " + user.getId(), e);
            }
            user.setProfilePictureUrl(profilePictureKey);
        }

        userRepository.save(user);
        return userMapper.toUserSelfProfileDto(user);
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        if (user.getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new SelfDeactivationException();
        }
        user.setActiveByMaster(false); // Corrected to use Lombok's generated setter
        userRepository.save(user);
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void activateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        user.setActiveByMaster(true); // Corrected to use Lombok's generated setter
        userRepository.save(user);
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        if (user.getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new SelfDeletionException();
        }
        userRepository.delete(user);
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void removeZoneFromUser(Long id, Long zoneId) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        Zone zone = zoneRepository.findById(zoneId).orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
        user.getZones().remove(zone);
        user.updateZoneCount();
        userRepository.save(user);
    }
    
    @Override
    @PreAuthorize("hasRole('MASTER')")
    public void promoteToMaster(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        Role masterRole = roleRepository.findByName("MASTER").orElseThrow(() -> new ResourceNotFoundException("MASTER role not found"));
        user.getRoles().add(masterRole);
        userRepository.save(user);
    }

    @PostConstruct
    @Transactional
    public void updateUserZoneCounts() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.updateZoneCount();
        }
        userRepository.saveAll(users);
    }
}
