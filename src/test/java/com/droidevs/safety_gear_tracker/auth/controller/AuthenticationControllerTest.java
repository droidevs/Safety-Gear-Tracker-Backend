package com.droidevs.safety_gear_tracker.auth.controller;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.auth.dtos.EmailVerificationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RegisterRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordRequest;
import com.droidevs.safety_gear_tracker.auth.service.AuthenticationService;
import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private WeeklyCodeService weeklyCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().name("USER").build();
        testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void register_shouldReturnOk_whenRegistrationIsSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password"
        );

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("token")
                .refreshToken("refreshToken")
                .expiresIn(3600L)
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void authenticate_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(
                "john.doe@example.com",
                "password"
        );

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("token")
                .refreshToken("refreshToken")
                .expiresIn(3600L)
                .build();
        
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void isAuthenticated_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is_authenticated"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isAuthenticated_notAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is_authenticated"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void isVerified_verified() throws Exception {
        when(authenticationService.isUserVerified("john.doe@example.com")).thenReturn(true);
        mockMvc.perform(get("/api/v1/auth/is_verified"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void sendVerifyOtp_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/send-otp"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void verifyUser_success() throws Exception {
        EmailVerificationRequest request = new EmailVerificationRequest("123456");
        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void changePassword_success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
