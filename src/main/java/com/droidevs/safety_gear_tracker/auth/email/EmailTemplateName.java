package com.droidevs.safety_gear_tracker.auth.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate-account"),
    FORGOT_PASSWORD("forgot-password"),
    FORCE_PASSWORD_RESET("force-password-reset");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
