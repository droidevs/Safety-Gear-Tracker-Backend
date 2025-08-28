package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Default to 404 for resource issues
public class ResourceBaseException extends GlobalBaseException {
    public ResourceBaseException(String message) {
        super(message);
    }

    public ResourceBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
