package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupServiceImpl implements CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupServiceImpl.class);
    private final UserRepository userRepository;

    public CleanupServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    @Transactional
    public void deleteUnverifiedUsers() {
        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<User> unverifiedUsers = userRepository.findByOtpVerifiedFalseAndCreatedAtBefore(threeDaysAgo);
            if (unverifiedUsers.isEmpty()) {
                logger.info("No unverified users to delete.");
                return;
            }

            logger.info("Deleting {} unverified users.", unverifiedUsers.size());
            for (User user : unverifiedUsers) {
                logger.info("Deleting user with email: {}", user.getEmail());
            }
            userRepository.deleteAll(unverifiedUsers);
            logger.info("Successfully deleted unverified users.");
        } catch (Exception e) {
            logger.error("An error occurred while deleting unverified users.", e);
        }
    }
}
