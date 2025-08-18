package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.model.SafetyGearType;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraResponseDto {
    private String id;
    private String name;
    private String ipAddress;
    private int port;
    private boolean active;
    private List<SafetyGearType> requiredSafetyGear; // Add required safety gear field
    // Exclude sensitive information like username and password in the response
    // Add other relevant settings as needed
}