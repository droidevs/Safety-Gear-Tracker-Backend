package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.WeeklyCode;

import java.util.List;
import java.util.Optional;

public interface WeeklyCodeService {
    void generateWeeklyCodes();
    void checkExpiredCodesAndLockAccounts();
    void validateCode(String email, String code);
    Optional<WeeklyCode> getLatestWeeklyCodeForUser(Long id);
}
