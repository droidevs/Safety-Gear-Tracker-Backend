package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;
import com.droidevs.safety_gear_tracker.auth.token.WeeklyCodeRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyCodeServiceImpl implements WeeklyCodeService {

    private final UserRepository userRepository;
    private final WeeklyCodeRepository weeklyCodeRepository;

    @Value("${master.user.email}")
    private String masterEmail;

    @Override
    @Scheduled(cron = "0 0 0 * * MON") // Runs every Monday at midnight
    public void generateWeeklyCodes() {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equals(masterEmail))
                .collect(Collectors.toList());
        for (User user : users) {
            String code = generateCode(6);
            WeeklyCode weeklyCode = WeeklyCode.builder()
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .user(user)
                    .build();
            weeklyCodeRepository.save(weeklyCode);
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void checkExpiredCodesAndLockAccounts() {
        List<WeeklyCode> codes = weeklyCodeRepository.findAll();
        for (WeeklyCode code : codes) {
            if (code.getValidatedAt() == null && LocalDateTime.now().isAfter(code.getExpiresAt())) {
                User user = code.getUser();
                if (!user.getEmail().equals(masterEmail)) {
                    user.setLocked(true);
                    userRepository.save(user);
                }
            }
        }
    }

    @Override
    public void validateCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeeklyCode weeklyCode = weeklyCodeRepository.findByUserAndCode(user, code)
                .orElseThrow(InvalidOtpException::new);

        if (LocalDateTime.now().isAfter(weeklyCode.getExpiresAt())) {
            throw new OtpExpiredException();
        }

        weeklyCode.setValidatedAt(LocalDateTime.now());
        weeklyCodeRepository.save(weeklyCode);
        
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
    
    @Override
    public Optional<WeeklyCode> getLatestWeeklyCodeForUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return weeklyCodeRepository.findTopByUserOrderByCreatedAtDesc(user);
    }
}
