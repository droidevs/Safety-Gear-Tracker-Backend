package com.droidevs.safety_gear_tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
}
