package com.droidevs.safety_gear_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDto {
    private Long id;
    private String cameraName;
    private String description;
    private LocalDateTime timestamp;
    private String screenshotUrl;
    private Long recordingId;
}
