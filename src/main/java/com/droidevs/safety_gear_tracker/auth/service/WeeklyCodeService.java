package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;

// BUG-04 FIX: removed unused `java.util.List` import — the interface has no method
// returning List; leaving it implied the interface was edited inconsistently.
import java.util.Optional;

public interface WeeklyCodeService {
    void generateWeeklyCodes();
    void checkExpiredCodesAndLockAccounts();
    void validateCode(String email, String code);
    Optional<WeeklyCode> getLatestWeeklyCodeForUser(Long id);
}