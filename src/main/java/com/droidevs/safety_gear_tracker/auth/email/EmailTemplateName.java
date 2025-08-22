package com.droidevs.safety_gear_tracker.auth.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account"),
    FORCE_PASSWORD_RESET("force_password_reset");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
