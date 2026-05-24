package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.Camera;
import java.time.LocalDateTime;
import java.util.List;

public record ViolationEvent(
        Camera camera,
        byte[] originalImage,
        List<SafetyViolation> violations,
        LocalDateTime timestamp
) {}
