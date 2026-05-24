package com.droidevs.safety_gear_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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
@ToString(exclude = {"camera", "alerts"})
@Entity
@Table(name = "recordings")
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @OneToMany(mappedBy = "recording")
    private List<Alert> alerts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recording recording = (Recording) o;
        return id != null && id.equals(recording.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
