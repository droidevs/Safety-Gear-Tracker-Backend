package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CameraManagementException extends GlobalBaseException {
    public CameraManagementException(String message) {
        super(message);
    }

    public CameraManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
