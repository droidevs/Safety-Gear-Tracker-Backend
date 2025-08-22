package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordChangeTooFrequentException extends RuntimeException {
    public PasswordChangeTooFrequentException() {
        super("You can only change your password once a week.");
    }
}
