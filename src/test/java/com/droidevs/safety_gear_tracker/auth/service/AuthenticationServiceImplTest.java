package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.config.JwtSecretInfo;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RegisterRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordOtpRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordRequest;
import com.droidevs.safety_gear_tracker.auth.email.EmailService;
import com.droidevs.safety_gear_tracker.auth.email.EmailTemplateName;
import com.droidevs.safety_gear_tracker.auth.token.Otp;
import com.droidevs.safety_gear_tracker.auth.token.OtpRepository;
import com.droidevs.safety_gear_tracker.auth.token.RefreshToken;
import com.droidevs.safety_gear_tracker.handler.exception.*;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.RoleRepository;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "activationUrl", "http://localhost:4200/activate-account");
        ReflectionTestUtils.setField(authenticationService, "fromAddress", "test@example.com");
        ReflectionTestUtils.setField(authenticationService, "masterEmail", "master@example.com");

        userRole = Role.builder().name("USER").build();
        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .otpVerified(false)
                .weeklyCodeVerified(false)
                .isActiveByMaster(true)
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void register_success() throws MessagingException {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "jane.doe@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpRepository.save(any(Otp.class))).thenReturn(Otp.builder().build());

        authenticationService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(
                eq("jane.doe@example.com"),
                eq("Activate Your Account"),
                eq(EmailTemplateName.ACTIVATE_ACCOUNT),
                anyMap(),
                eq("test@example.com")
        );
    }

    @Test
    void register_userAlreadyExists() throws MessagingException {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "john.doe@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyMap(), anyString());
    }

    @Test
    void register_masterUserEmail() throws MessagingException {
        RegisterRequest request = new RegisterRequest("Master", "User", "master@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_success() {
        AuthenticationRequest request = new AuthenticationRequest("john.doe@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwtToken");
        
        JwtSecretInfo jwtSecretInfo = new JwtSecretInfo();
        jwtSecretInfo.setSecretKey("secret");
        jwtSecretInfo.setExpirationTime(3600);
        when(jwtService.getJwtSecretInfo()).thenReturn(jwtSecretInfo);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(RefreshToken.builder().token("refreshToken").build());

        authenticationService.authenticate(request);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(anyMap(), any(User.class));
    }

    @Test
    void authenticate_userNotFound() {
        AuthenticationRequest request = new AuthenticationRequest("nonexistent@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticationService.authenticate(request));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_masterUserLoginAttempt() {
        AuthenticationRequest request = new AuthenticationRequest("master@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser)); // Master user exists

        assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(request));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void sendOtp_success() throws MessagingException {
        testUser.setOtpVerified(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.save(any(Otp.class))).thenReturn(Otp.builder().build());

        authenticationService.sendOtp("john.doe@example.com");

        verify(emailService, times(1)).sendEmail(
                eq("john.doe@example.com"),
                eq("Activate Your Account"),
                eq(EmailTemplateName.ACTIVATE_ACCOUNT),
                anyMap(),
                eq("test@example.com")
        );
    }

    @Test
    void sendOtp_userNotFound() throws MessagingException {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticationService.sendOtp("nonexistent@example.com"));
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyMap(), anyString());
    }

    @Test
    void sendOtp_accountAlreadyVerified() throws MessagingException {
        testUser.setOtpVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(AccountAlreadyVerifiedException.class, () -> authenticationService.sendOtp("john.doe@example.com"));
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyMap(), anyString());
    }

    @Test
    void verifyUser_success() {
        Otp otp = Otp.builder()
                .otp("123456")
                .user(testUser)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("123456"))).thenReturn(Optional.of(otp));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpRepository.save(any(Otp.class))).thenReturn(otp);

        authenticationService.verifyUser("john.doe@example.com", "123456");

        assertTrue(testUser.isOtpVerified());
        assertNotNull(otp.getValidatedAt());
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpRepository, times(1)).save(any(Otp.class));
    }

    @Test
    void verifyUser_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticationService.verifyUser("nonexistent@example.com", "123456"));
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verifyUser_invalidOtp() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("wrongOtp"))).thenReturn(Optional.empty());

        assertThrows(InvalidOtpException.class, () -> authenticationService.verifyUser("john.doe@example.com", "wrongOtp"));
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verifyUser_otpExpired() {
        Otp otp = Otp.builder()
                .otp("123456")
                .user(testUser)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // Expired
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("123456"))).thenReturn(Optional.of(otp));

        assertThrows(OtpExpiredException.class, () -> authenticationService.verifyUser("john.doe@example.com", "123456"));
        assertFalse(testUser.isOtpVerified());
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void isUserVerified_true() {
        testUser.setOtpVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertTrue(authenticationService.isUserVerified("john.doe@example.com"));
    }

    @Test
    void isUserVerified_false() {
        testUser.setOtpVerified(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertFalse(authenticationService.isUserVerified("john.doe@example.com"));
    }

    @Test
    void isUserVerified_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authenticationService.isUserVerified("nonexistent@example.com"));
    }

    @Test
    void changePassword_success() {
        testUser.setPassword("encodedOldPassword"); // Set encoded password for test user
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword"), eq("encodedOldPassword"))).thenReturn(true); // Match against the set password
        when(passwordEncoder.encode(eq("newPassword"))).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        authenticationService.changePassword(request, "john.doe@example.com");

        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        assertThrows(UserNotFoundException.class, () -> authenticationService.changePassword(request, "nonexistent@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_invalidOldPassword() {
        testUser.setPassword("encodedCorrectOldPassword"); // Set encoded password for test user
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("wrongOldPassword"), eq("encodedCorrectOldPassword"))).thenReturn(false); // Match against the set password

        ResetPasswordRequest request = new ResetPasswordRequest("wrongOldPassword", "newPassword");
        assertThrows(InvalidPasswordException.class, () -> authenticationService.changePassword(request, "john.doe@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_masterUser_passwordChangeTooFrequent() {
        testUser.setEmail("master@example.com"); // Simulate master user
        testUser.setLastPasswordChange(LocalDateTime.now().minusDays(1)); // Changed recently
        testUser.setPassword("encodedOldPassword"); // Set encoded password for test user

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        // Removed unnecessary stubbing for passwordEncoder.matches as it's not reached

        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        assertThrows(PasswordChangeTooFrequentException.class, () -> authenticationService.changePassword(request, "master@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_masterUser_passwordChangeAllowed() {
        testUser.setEmail("master@example.com"); // Simulate master user
        testUser.setLastPasswordChange(LocalDateTime.now().minusWeeks(2)); // Changed long ago
        testUser.setPassword("encodedOldPassword"); // Set encoded password for test user

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword"), eq("encodedOldPassword"))).thenReturn(true); // Match against the set password
        when(passwordEncoder.encode(eq("newPassword"))).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResetPasswordRequest request = new ResetPasswordRequest("oldPassword", "newPassword");
        authenticationService.changePassword(request, "master@example.com");

        assertEquals("encodedNewPassword", testUser.getPassword());
        assertNotNull(testUser.getLastPasswordChange());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void resetPasswordOtp_success() {
        Otp otp = Otp.builder()
                .otp("123456")
                .user(testUser)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("123456"))).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode(eq("newPassword"))).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpRepository.save(any(Otp.class))).thenReturn(otp);

        ResetPasswordOtpRequest request = new ResetPasswordOtpRequest("john.doe@example.com", "123456", "newPassword");
        authenticationService.resetPasswordOtp(request);

        assertEquals("encodedNewPassword", testUser.getPassword());
        assertNotNull(otp.getValidatedAt());
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpRepository, times(1)).save(any(Otp.class));
    }

    @Test
    void resetPasswordOtp_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResetPasswordOtpRequest request = new ResetPasswordOtpRequest("nonexistent@example.com", "123456", "newPassword");
        assertThrows(UserNotFoundException.class, () -> authenticationService.resetPasswordOtp(request));
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void resetPasswordOtp_invalidOtp() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("wrongOtp"))).thenReturn(Optional.empty());

        ResetPasswordOtpRequest request = new ResetPasswordOtpRequest("john.doe@example.com", "wrongOtp", "newPassword");
        assertThrows(InvalidOtpException.class, () -> authenticationService.resetPasswordOtp(request));
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void resetPasswordOtp_otpExpired() {
        Otp otp = Otp.builder()
                .otp("123456")
                .user(testUser)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // Expired
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpRepository.findByUserAndOtp(eq(testUser), eq("123456"))).thenReturn(Optional.of(otp));

        ResetPasswordOtpRequest request = new ResetPasswordOtpRequest("john.doe@example.com", "123456", "newPassword");
        assertThrows(OtpExpiredException.class, () -> authenticationService.resetPasswordOtp(request));
        verify(userRepository, never()).save(any(User.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }
}
