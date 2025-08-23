package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.SafetyViolation;
import com.droidevs.safety_gear_tracker.model.Camera;

import java.util.List;

public interface SafetyGearDetectionService {
    List<SafetyViolation> findViolations(byte[] imageData, Camera camera);
}
