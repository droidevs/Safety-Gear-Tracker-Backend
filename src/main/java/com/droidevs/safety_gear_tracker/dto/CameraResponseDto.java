package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraResponseDto {
    private Long id;
    private String name;
    private String ipAddress;
    private int port;
    private String rtspUrl;
    private boolean active;
    private boolean isRecordingActive;
    private Long zoneId;
    private List<SafetyGearType> requiredSafetyGear;
    private String username;
}
