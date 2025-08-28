package com.droidevs.safety_gear_tracker.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfDeletionException extends AuthenticationBaseException {
    public SelfDeletionException() {
        super("You cannot delete your own account.");
    }
}
