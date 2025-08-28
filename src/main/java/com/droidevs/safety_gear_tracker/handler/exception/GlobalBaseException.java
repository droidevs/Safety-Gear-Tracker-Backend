package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Default to 500 if not overridden
public class GlobalBaseException extends RuntimeException {
    public GlobalBaseException(String message) {
        super(message);
    }

    public GlobalBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
