package com.droidevs.safety_gear_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cameraName;
    private String description;
    private LocalDateTime timestamp;
    private String screenshotUrl;

    @ManyToOne
    @JoinColumn(name = "camera_id")
    private Camera camera;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recording_id", referencedColumnName = "id")
    private Recording recording;
}
