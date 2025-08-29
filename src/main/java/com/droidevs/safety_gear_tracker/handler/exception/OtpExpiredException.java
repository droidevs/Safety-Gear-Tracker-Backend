package com.droidevs.safety_gear_tracker.handler.exception;

public class OtpExpiredException extends AuthenticationBaseException {
    public OtpExpiredException() {
        super("The OTP has expired. Please request a new one.");
    }
}
