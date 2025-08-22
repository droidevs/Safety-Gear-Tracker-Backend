package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyUsedOtpException extends RuntimeException {
    public AlreadyUsedOtpException() {
        super("This OTP has already been used.");
    }
}
