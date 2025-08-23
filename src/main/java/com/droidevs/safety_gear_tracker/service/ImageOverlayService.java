package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.SafetyViolation;

import java.util.List;

public interface ImageOverlayService {
    byte[] drawViolationsOnImage(byte[] originalImage, List<SafetyViolation> violations);
}
