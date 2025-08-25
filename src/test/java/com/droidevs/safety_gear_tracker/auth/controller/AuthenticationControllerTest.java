
package com.droidevs.safety_gear_tracker.auth.controller;

import com.droidevs.safety_gear_tracker.auth.dtos.*;
import com.droidevs.safety_gear_tracker.auth.service.AuthenticationService;
import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @MockBean
    private AuthenticationService service;

    @MockBean
    private WeeklyCodeService weeklyCodeService;

    @MockBean
    private GenerativeModel generativeModel;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String userEmail;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("USER"));
        userEmail = "john.doe@example.com";
        user = new User("John", "Doe", userEmail, "password", true, roles);
    }

    @Test
    void isAuthenticated_shouldReturnOk_whenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is_authenticated")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities()))))
                .andExpect(status().isOk());
    }

    @Test
    void isVerified_shouldReturnOk_whenUserIsVerified() throws Exception {
        when(service.isUserVerified(userEmail)).thenReturn(true);
        mockMvc.perform(get("/api/v1/auth/is_verified")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities()))))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldReturnOk_whenRegistrationIsSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john.doe@example.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("token", 3600L);
        when(service.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("john.doe@example.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("token", 3600L);
        when(service.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void sendVerifyOtp_shouldReturnOk_whenOtpIsSent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities()))))
                .andExpect(status().isOk());
    }

    @Test
    void verifyUser_shouldReturnOk_whenOtpIsValid() throws Exception {
        EmailVerificationRequest request = new EmailVerificationRequest("123456");

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void validateDailyCode_shouldReturnOk_whenCodeIsValid() throws Exception {
        DailyCodeValidationRequest request = new DailyCodeValidationRequest("123456");
        doNothing().when(weeklyCodeService).validateCode(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/validate-daily-code")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_shouldReturnOk_whenRequestIsValid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        doNothing().when(service).resetPassword(any(ResetPasswordRequest.class), anyString());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userEmail, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
