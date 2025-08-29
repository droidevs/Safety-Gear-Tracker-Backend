package com.droidevs.safety_gear_tracker.auth.controller;

import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.droidevs.safety_gear_tracker.controller.UserController;
import com.droidevs.safety_gear_tracker.dto.UpdateProfileRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserPagingRequestDto;
import com.droidevs.safety_gear_tracker.dto.UserProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSelfProfileDto;
import com.droidevs.safety_gear_tracker.dto.UserSummaryPagingResponseDto;
import com.droidevs.safety_gear_tracker.dto.UserSummaryResponseDto;
import com.droidevs.safety_gear_tracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    
    @Mock
    private WeeklyCodeService weeklyCodeService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void updateProfile_shouldUpdateUserProfile() throws Exception {
        MockMultipartFile profilePicture = new MockMultipartFile("profilePicture", "test.jpg", "image/jpeg", "test image content".getBytes());
        UserSelfProfileDto userSelfProfileDto = new UserSelfProfileDto(1L, "John", "Doe", "john.doe@example.com", "avatar.jpg", true, false, false, true);

        when(userService.updateMyProfile(any(UpdateProfileRequestDto.class))).thenReturn(userSelfProfileDto);

        mockMvc.perform(multipart("/users/profile/me")
                        .file(profilePicture)
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        UserSelfProfileDto userSelfProfileDto = new UserSelfProfileDto(1L, "John", "Doe", "john.doe@example.com", "avatar.jpg", true, false, false, true);

        when(userService.getMyProfile()).thenReturn(userSelfProfileDto);

        mockMvc.perform(get("/users/profile/me"))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_shouldReturnUsers() throws Exception {
        UserPagingRequestDto requestDto = new UserPagingRequestDto();
        Page<UserSummaryResponseDto> userPage = new PageImpl<>(Collections.emptyList());
        UserSummaryPagingResponseDto responseDto = new UserSummaryPagingResponseDto(userPage);

        when(userService.getAllUsers(any(UserPagingRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Long userId = 1L;
        UserProfileDto userProfileDto = new UserProfileDto(userId, "John", "Doe", "john.doe@example.com", true, false, true, null, "avatar.jpg");

        when(userService.getUser(userId)).thenReturn(userProfileDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignZonesToUser_shouldAssignZones() throws Exception {
        Long userId = 1L;
        Set<Long> zoneIds = Set.of(1L, 2L);
        doNothing().when(userService).assignZonesToUser(userId, zoneIds);

        mockMvc.perform(post("/users/{id}/zones", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneIds)))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateUser_shouldDeactivateUser() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deactivateUser(userId);

        mockMvc.perform(put("/users/{id}/deactivate", userId))
                .andExpect(status().isOk());
    }
    
    @Test
    void activateUser_shouldActivateUser() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).activateUser(userId);
        
        mockMvc.perform(put("/users/{id}/activate", userId))
                .andExpect(status().isOk());
    }
    
    @Test
    void removeZoneFromUser_shouldRemoveZone() throws Exception {
        Long userId = 1L;
        Long zoneId = 1L;
        doNothing().when(userService).removeZoneFromUser(userId, zoneId);
        
        mockMvc.perform(delete("/users/{id}/zones/{zoneId}", userId, zoneId))
                .andExpect(status().isOk());
    }
    
    @Test
    void getLatestWeeklyCodeForUser_shouldReturnCode() throws Exception {
        Long userId = 1L;
        WeeklyCode weeklyCode = new WeeklyCode();
        when(weeklyCodeService.getLatestWeeklyCodeForUser(userId)).thenReturn(Optional.of(weeklyCode));
        
        mockMvc.perform(get("/users/{id}/latest-weekly-code", userId))
                .andExpect(status().isOk());
    }

    @Test
    void promoteToMaster_shouldPromoteUser() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).promoteToMaster(userId);
        
        mockMvc.perform(put("/users/{id}/promote", userId))
                .andExpect(status().isOk());
    }
}