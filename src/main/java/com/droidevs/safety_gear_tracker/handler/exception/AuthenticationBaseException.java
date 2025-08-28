package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Default to 400 for authentication issues
public class AuthenticationBaseException extends GlobalBaseException {
    public AuthenticationBaseException(String message) {
        super(message);
    }

    public AuthenticationBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
