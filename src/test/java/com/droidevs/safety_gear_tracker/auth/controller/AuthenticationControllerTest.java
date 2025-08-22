package com.droidevs.safety_gear_tracker.auth.controller;

import com.droidevs.safety_gear_tracker.auth.config.SecurityConfigTest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.auth.dtos.EmailVerificationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RegisterRequest;
import com.droidevs.safety_gear_tracker.auth.service.AuthenticationService;
import com.droidevs.safety_gear_tracker.auth.service.DailyCodeService;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = SecurityConfigTest.class)
class AuthenticationControllerTest {

    @MockBean
    private AuthenticationService service;

    @MockBean
    private DailyCodeService dailyCodeService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("USER"));
        user = new User("John", "Doe", "john.doe@example.com", "password", true, roles);
    }

    @Test
    void isAuthenticated_shouldReturnOk_whenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is_authenticated")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))))
                .andExpect(status().isOk());
    }

    @Test
    void isVerified_shouldReturnOk_whenUserIsVerified() throws Exception {
        when(service.isUserVerified("john.doe@example.com")).thenReturn(true);
        mockMvc.perform(get("/api/v1/auth/is_verified")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))))
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
    void sendVerifyOtp_shouldReturnOk_whenOtpIsSent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))))
                .andExpect(status().isOk());
    }

    @Test
    void verifyUser_shouldReturnOk_whenOtpIsValid() throws Exception {
        EmailVerificationRequest request = new EmailVerificationRequest("123456");

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
