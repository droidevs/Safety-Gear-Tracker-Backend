package com.droidevs.safety_gear_tracker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void sendAlert(String subject, String message, Throwable cause) {
        log.error("*** ALERT: {} ***", subject);
        log.error("Message: {}", message);
        log.error("Cause: {}: {}", cause.getClass().getSimpleName(), cause.getMessage(), cause);
        // In a real implementation, you'd send an email, a Slack message, etc.
    }
}
