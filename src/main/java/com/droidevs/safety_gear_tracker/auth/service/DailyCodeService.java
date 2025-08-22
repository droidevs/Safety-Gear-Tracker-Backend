package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.DailyCode;
import com.droidevs.safety_gear_tracker.auth.token.DailyCodeRepository;
import com.droidevs.safety_gear_tracker.handler.exception.InvalidOtpException;
import com.droidevs.safety_gear_tracker.handler.exception.OtpExpiredException;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyCodeService {

    private final UserRepository userRepository;
    private final DailyCodeRepository dailyCodeRepository;

    @Value("${master.user.email}")
    private String masterEmail;

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void generateDailyCodes() {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equals(masterEmail))
                .collect(Collectors.toList());
        for (User user : users) {
            String code = generateCode(6);
            DailyCode dailyCode = DailyCode.builder()
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .user(user)
                    .build();
            dailyCodeRepository.save(dailyCode);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void checkExpiredCodesAndLockAccounts() {
        List<DailyCode> codes = dailyCodeRepository.findAll();
        for (DailyCode code : codes) {
            if (code.getValidatedAt() == null && LocalDateTime.now().isAfter(code.getExpiresAt())) {
                User user = code.getUser();
                if (!user.getEmail().equals(masterEmail)) {
                    user.setLocked(true);
                    userRepository.save(user);
                }
            }
        }
    }

    public void validateCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DailyCode dailyCode = dailyCodeRepository.findByUserAndCode(user, code)
                .orElseThrow(InvalidOtpException::new);

        if (LocalDateTime.now().isAfter(dailyCode.getExpiresAt())) {
            throw new OtpExpiredException();
        }

        dailyCode.setValidatedAt(LocalDateTime.now());
        dailyCodeRepository.save(dailyCode);
        
        user.setLocked(false);
        userRepository.save(user);
    }

    private String generateCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
    
    public List<DailyCode> getDailyCodesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return dailyCodeRepository.findByUser(user);
    }
}
