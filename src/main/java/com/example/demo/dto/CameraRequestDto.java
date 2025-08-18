package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.model.SafetyGearType;
import java.util.List;

@Data
@NoArgsConstructor
public class CameraRequestDto {
    private String name;
    private String ipAddress;
    private int port;
    private String username;
    private String password;
    private boolean active; // Add active field
    private List<SafetyGearType> requiredSafetyGear; // Add required safety gear field
    // Add other relevant settings as needed
}