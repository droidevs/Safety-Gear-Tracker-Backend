package com.droidevs.safety_gear_tracker.config.async;

import com.droidevs.safety_gear_tracker.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.lang.reflect.Method;

@Component
@Slf4j
public class CustomTaskErrorHandler implements ErrorHandler, AsyncUncaughtExceptionHandler {

    private final NotificationService notificationService;

    public CustomTaskErrorHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void handleError(Throwable t) {
        String errorMessage = String.format("Unhandled exception in @Scheduled task or TaskScheduler: %s", t.getMessage());
        log.error(errorMessage, t);
        notificationService.sendAlert("Critical Scheduled Task Failure", errorMessage, t);
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        StringBuilder paramInfo = new StringBuilder();
        for (Object param : params) {
            paramInfo.append(param != null ? param.toString() : "null").append(", ");
        }
        String errorMessage = String.format("Unhandled exception in @Async method: %s with params [%s] - %s",
                method.getName(), paramInfo.toString(), ex.getMessage());
        log.error(errorMessage, ex);
        notificationService.sendAlert("Critical Async Method Failure", errorMessage, ex);
    }
}
