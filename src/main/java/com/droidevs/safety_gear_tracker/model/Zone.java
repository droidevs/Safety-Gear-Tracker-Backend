package com.droidevs.safety_gear_tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"users", "cameras"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String description;

    @Column(name = "user_count")
    private Integer userCount;

    @Column(name = "camera_count")
    private Integer cameraCount;

    @ManyToMany(mappedBy = "zones")
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "zone")
    @JsonIgnore
    private Set<Camera> cameras;

    @PrePersist
    @PreUpdate
    private void updateCounts() {
        if (users != null) {
            userCount = users.size();
        } else {
            userCount = 0;
        }
        if (cameras != null) {
            cameraCount = cameras.size();
        } else {
            cameraCount = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Zone)) return false;
        Zone zone = (Zone) o;
        return id != null && id.equals(zone.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
