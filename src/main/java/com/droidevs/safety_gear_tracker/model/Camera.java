package com.droidevs.safety_gear_tracker.model;

import com.droidevs.safety_gear_tracker.service.HikvisionRtspUrlBuilder;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String ipAddress;
    private int port;
    private String username;
    private String password;
    private boolean active;
    private boolean recordingActive;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<SafetyGearType> requiredSafetyGear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public String getStreamUrl() {
        HikvisionRtspUrlBuilder builder = new HikvisionRtspUrlBuilder(ipAddress, port, username, password);
       return builder.buildRtspUrl(1, true);
    }
}
