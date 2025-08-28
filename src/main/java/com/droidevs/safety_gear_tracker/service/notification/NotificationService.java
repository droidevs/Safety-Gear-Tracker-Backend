package com.droidevs.safety_gear_tracker.service.notification;

public interface NotificationService {
    void sendAlert(String subject, String message, Throwable cause);
}