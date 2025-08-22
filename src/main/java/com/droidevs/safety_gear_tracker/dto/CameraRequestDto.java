package com.droidevs.safety_gear_tracker.dto;

import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CameraRequestDto {
    private String name;
    private String ipAddress;
    private int port;
    private String username;
    private String password;
    private String rtspUrl;
    private boolean active;
    private boolean isRecordingActive;
    private Long zoneId;
    private List<SafetyGearType> requiredSafetyGear;
}
