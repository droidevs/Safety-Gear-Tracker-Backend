package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException() {
        super("The OTP has expired. Please request a new one.");
    }
}
