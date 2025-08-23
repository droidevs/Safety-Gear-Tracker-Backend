package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.DailyCode;

import java.util.List;

public interface DailyCodeService {
    void generateDailyCodes();
    void checkExpiredCodesAndLockAccounts();
    void validateCode(String email, String code);
    List<DailyCode> getDailyCodesForUser(String email);
}
