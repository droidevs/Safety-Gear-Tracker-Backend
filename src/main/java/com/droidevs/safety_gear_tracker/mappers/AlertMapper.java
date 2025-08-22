package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.model.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponseDto toDto(Alert alert) {
        return new AlertResponseDto(
                alert.getId(),
                alert.getCameraName(),
                alert.getDescription(),
                alert.getTimestamp(),
                alert.getScreenshotUrl(),
                alert.getRecording() != null ? alert.getRecording().getId() : null
        );
    }
}
