package com.droidevs.safety_gear_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"camera", "recording"})
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

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendations;

    @ManyToOne
    @JoinColumn(name = "camera_id")
    private Camera camera;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recording_id", referencedColumnName = "id")
    private Recording recording;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return id != null && id.equals(alert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
