package com.droidevs.safety_gear_tracker.model;

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
import jakarta.persistence.ManyToOne;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int port;
    private String username;
    private String password;
    private String rtspUrl;
    private String streamUrl;
    private boolean active = true;
    private boolean isRecordingActive = false;
    @ManyToOne
    private Zone zone;
    private String name;
    private String ipAddress;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<SafetyGearType> requiredSafetyGear;
}
