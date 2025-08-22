package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super("The OTP provided is invalid or has expired.");
    }
}
