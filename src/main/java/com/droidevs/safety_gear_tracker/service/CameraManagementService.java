package com.droidevs.safety_gear_tracker.service;

import org.springframework.util.StringUtils;

public interface CameraManagementService {
    boolean changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword);

    default boolean validateCredentials(String... credentials) {
        for (String credential : credentials) {
            if (!StringUtils.hasText(credential)) {
                return false;
            }
        }
        return true;
    }
}
