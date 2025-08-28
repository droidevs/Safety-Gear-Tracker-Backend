package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.handler.exception.CameraManagementException;
import org.springframework.util.StringUtils;

public interface CameraManagementService {
    void changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword) throws CameraManagementException;

    default boolean validateCredentials(String... credentials) {
        for (String credential : credentials) {
            if (!StringUtils.hasText(credential)) {
                return false;
            }
        }
        return true;
    }
}
