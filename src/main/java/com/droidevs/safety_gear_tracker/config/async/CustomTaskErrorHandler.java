package com.droidevs.safety_gear_tracker.config.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.lang.reflect.Method;

@Component
@Slf4j
public class CustomTaskErrorHandler implements ErrorHandler, AsyncUncaughtExceptionHandler {

    @Override
    public void handleError(Throwable t) {
        log.error("Unhandled exception in @Scheduled task or TaskScheduler: {}", t.getMessage(), t);
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Unhandled exception in @Async method: {} - {}", method.getName(), ex.getMessage(), ex);
        for (Object param : params) {
            log.error("Parameter value: {}", param);
        }
    }
}
