package com.droidevs.safety_gear_tracker.handler.exception;

public class AlreadyUsedOtpException extends AuthenticationBaseException {
    public AlreadyUsedOtpException() {
        super("This OTP has already been used. Please request a new one.");
    }
}
