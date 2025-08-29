package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.auth.dtos.ForgotPasswordRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RefreshTokenRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RegisterRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordOtpRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordRequest;
import com.droidevs.safety_gear_tracker.auth.email.EmailService;
import com.droidevs.safety_gear_tracker.auth.email.EmailTemplateName;
import com.droidevs.safety_gear_tracker.auth.token.Otp;
import com.droidevs.safety_gear_tracker.auth.token.OtpRepository;
import com.droidevs.safety_gear_tracker.auth.token.RefreshToken;
import com.droidevs.safety_gear_tracker.auth.token.RefreshTokenRepository;
import com.droidevs.safety_gear_tracker.handler.exception.AccountAlreadyVerifiedException;
import com.droidevs.safety_gear_tracker.handler.exception.AlreadyUsedOtpException;
import com.droidevs.safety_gear_tracker.handler.exception.InvalidOtpException;
import com.droidevs.safety_gear_tracker.handler.exception.InvalidPasswordException;
import com.droidevs.safety_gear_tracker.handler.exception.OtpExpiredException;
import com.droidevs.safety_gear_tracker.handler.exception.PasswordChangeTooFrequentException;
import com.droidevs.safety_gear_tracker.handler.exception.TokenRefreshException;
import com.droidevs.safety_gear_tracker.handler.exception.UserAlreadyExistsException;
import com.droidevs.safety_gear_tracker.handler.exception.UserNotFoundException;
import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.RoleRepository;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${master.user.email}")
    private String masterEmail;

    @Override
    public AuthenticationResponse register(RegisterRequest request) throws MessagingException {
        if (userRepository.findByEmail(request.email()).isPresent() || request.email().equals(masterEmail)) {
            throw new UserAlreadyExistsException();
        }

        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("USER role not found"));
        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .otpVerified(false)
                .weeklyCodeVerified(false)
                .isActiveByMaster(false) // Added isActiveByMaster
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        sendVerificationEmail(user);
        
        return new AuthenticationResponse(null, 0, null);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (request.email().equals(masterEmail)) {
            throw new IllegalArgumentException("Master user cannot log in through this endpoint.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) auth.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());

        String jwtToken = jwtService.generateToken(claims, user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthenticationResponse(jwtToken, jwtService.getJwtSecretInfo().getExpirationTime(), refreshToken.getToken());
    }
    
    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return new AuthenticationResponse(token, jwtService.getJwtSecretInfo().getExpirationTime(), requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @Override
    public void sendOtp(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (user.isOtpVerified()) { // Changed from isEnabled() to isOtpVerified()
            throw new AccountAlreadyVerifiedException();
        }
        sendVerificationEmail(user);
    }

    @Override
    public void verifyUser(String email, String otp) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Otp savedOtp = otpRepository.findByUserAndOtp(user, otp).orElseThrow(InvalidOtpException::new);

        if (LocalDateTime.now().isAfter(savedOtp.getExpiresAt())) {
            throw new OtpExpiredException();
        }

        if (savedOtp.getValidatedAt() != null) {
            throw new AlreadyUsedOtpException();
        }

        user.setOtpVerified(true); // Removed user.setEnabled(true)
        userRepository.save(user);
        savedOtp.setValidatedAt(LocalDateTime.now());
        otpRepository.save(savedOtp);
    }

    @Override
    public boolean isUserVerified(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return user.isOtpVerified(); // Changed from isEnabled() to isOtpVerified()
    }
    
    @Override
    public void changePassword(ResetPasswordRequest request, String email) {
        if (email == null || email.isEmpty()){
            throw new UserNotFoundException();
        }
        var user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (email.equals(masterEmail)) {
            if (user.getLastPasswordChange() != null && user.getLastPasswordChange().isAfter(LocalDateTime.now().minusWeeks(1))) {
                throw new PasswordChangeTooFrequentException();
            }
        }
        
        if (passwordEncoder.matches(request.oldPassword(), user.getPassword())){
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            if(email.equals(masterEmail)) {
                user.setLastPasswordChange(LocalDateTime.now());
            }
            userRepository.save(user);
        } else {
            throw new InvalidPasswordException();
        }
    }
    
    @Override
    public void resetPasswordOtp(ResetPasswordOtpRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(UserNotFoundException::new);
        Otp savedOtp = otpRepository.findByUserAndOtp(user, request.otp()).orElseThrow(InvalidOtpException::new);
        
        if (LocalDateTime.now().isAfter(savedOtp.getExpiresAt())) {
            throw new OtpExpiredException();
        }

        if (savedOtp.getValidatedAt() != null) {
            throw new AlreadyUsedOtpException();
        }
        
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        savedOtp.setValidatedAt(LocalDateTime.now());
        otpRepository.save(savedOtp);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.email()).orElseThrow(UserNotFoundException::new);
        sendPasswordResetEmail(user);
    }
    
    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        refreshTokenRepository.deleteByUser(user);
    }

    private void sendVerificationEmail(User user) throws MessagingException {
        String generatedOtp = generateAndSaveActivationOtp(user);
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", user.getFullName());
        properties.put("confirmationUrl", activationUrl);
        properties.put("activation_code", generatedOtp);

        emailService.sendEmail(
                user.getEmail(),
                "Activate Your Account",
                EmailTemplateName.ACTIVATE_ACCOUNT,
                properties,
                fromAddress
        );
    }

    private void sendPasswordResetEmail(User user) throws MessagingException {
        String generatedOtp = generateAndSaveActivationOtp(user);
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", user.getFullName());
        properties.put("activation_code", generatedOtp);

        emailService.sendEmail(
                user.getEmail(),
                "Reset Your Password",
                EmailTemplateName.FORGOT_PASSWORD,
                properties,
                fromAddress
        );
    }

    private String generateAndSaveActivationOtp(User user) {
        otpRepository.findByUser(user).ifPresent(otpRepository::delete); // Invalidate existing OTP
        String generatedOtp = generateActivationCode(6);
        Otp otp = Otp.builder()
                .otp(generatedOtp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        otpRepository.save(otp);
        return generatedOtp;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
