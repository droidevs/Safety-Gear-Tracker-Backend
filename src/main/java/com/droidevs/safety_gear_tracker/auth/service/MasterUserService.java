package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.email.EmailService;
import com.droidevs.safety_gear_tracker.auth.email.EmailTemplateName;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MasterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${master.user.email}")
    private String masterEmail;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void forceMasterPasswordChangeIfNeeded() throws MessagingException {
        User masterUser = userRepository.findByEmail(masterEmail).orElse(null);

        if (masterUser != null && isPasswordChangeRequired(masterUser)) {
            String newPassword = generateRandomPassword(12);
            masterUser.setPassword(passwordEncoder.encode(newPassword));
            masterUser.setLastPasswordChange(LocalDateTime.now());
            userRepository.save(masterUser);

            sendForcedPasswordResetEmail(masterUser, newPassword);
        }
    }

    private boolean isPasswordChangeRequired(User user) {
        if (user.getLastPasswordChange() == null) {
            // If password has never been changed, it doesn't need a forced reset yet.
            // The first change will be initiated by the user.
            return false;
        }
        return user.getLastPasswordChange().isBefore(LocalDateTime.now().minusWeeks(1));
    }

    private void sendForcedPasswordResetEmail(User user, String newPassword) throws MessagingException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", user.getFullName());
        properties.put("new_password", newPassword);

        emailService.sendEmail(
                user.getEmail(),
                "Important: Your Master Account Password Has Been Reset",
                EmailTemplateName.FORCE_PASSWORD_RESET,
                properties,
                fromAddress
        );
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
}
