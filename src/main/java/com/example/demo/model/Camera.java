package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Camera {
    @Id // Define as primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate ID
    private String id; // Unique identifier for the camera
    private int port;
    private String username;
    private String password;
    private boolean active = true; // New field for active status

    @ElementCollection // For collections of basic types or enums
    @Enumerated(EnumType.STRING) // Store enum as String
    private List<SafetyGearType> requiredSafetyGear; // List of required safety gear
    private String name;
 private String ipAddress;
}