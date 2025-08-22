
package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.dtos.*;
import com.droidevs.safety_gear_tracker.auth.email.EmailService;
import com.droidevs.safety_gear_tracker.auth.email.EmailTemplateName;
import com.droidevs.safety_gear_tracker.auth.token.Otp;
import com.droidevs.safety_gear_tracker.auth.token.OtpRepository;
import com.droidevs.safety_gear_tracker.handler.exception.*;
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
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${master.user.email}")
    private String masterEmail;

    public AuthenticationResponse register(RegisterRequest request) throws MessagingException {
        if (userRepository.findByEmail(request.getEmail()).isPresent() || request.getEmail().equals(masterEmail)) {
            throw new UserAlreadyExistsException();
        }

        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("USER role not found"));
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .locked(false)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        sendVerificationEmail(user);
        
        return AuthenticationResponse.builder().build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (request.getEmail().equals(masterEmail)) {
            throw new IllegalArgumentException("Master user cannot log in through this endpoint.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) auth.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());

        String jwtToken = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getJwtSecretInfo().getExpiration_time())
                .build();
    }
    
    public void sendOtp(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException();
        }
        sendVerificationEmail(user);
    }

    public void verifyUser(String email, String otp) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Otp savedOtp = otpRepository.findByUserAndOtp(user, otp).orElseThrow(InvalidOtpException::new);

        if (LocalDateTime.now().isAfter(savedOtp.getExpiresAt())) {
            throw new OtpExpiredException();
        }

        user.setEnabled(true);
        userRepository.save(user);
        savedOtp.setValidatedAt(LocalDateTime.now());
        otpRepository.save(savedOtp);
    }

    public boolean isUserVerified(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return user.isEnabled();
    }
    
    public void resetPassword(ResetPasswordRequest request, String email) {
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
        
        if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            if(email.equals(masterEmail)) {
                user.setLastPasswordChange(LocalDateTime.now());
            }
            userRepository.save(user);
        } else {
            throw new InvalidPasswordException();
        }
    }
    
    public void resetPasswordOtp(ResetPasswordOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);
        Otp savedOtp = otpRepository.findByUserAndOtp(user, request.getOtp()).orElseThrow(InvalidOtpException::new);
        
        if (LocalDateTime.now().isAfter(savedOtp.getExpiresAt())) {
            throw new OtpExpiredException();
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        savedOtp.setValidatedAt(LocalDateTime.now());
        otpRepository.save(savedOtp);
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

    private String generateAndSaveActivationOtp(User user) {
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
