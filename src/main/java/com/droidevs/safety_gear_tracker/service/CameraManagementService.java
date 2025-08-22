package com.droidevs.safety_gear_tracker.service;

public interface CameraManagementService {
    boolean changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword);
}
