package com.droidevs.safety_gear_tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Zone name cannot be blank")
    private String name;

    private String description;

    private Integer userCount;
    private Integer cameraCount;

    @ManyToMany(mappedBy = "zones", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Camera> cameras;

    @PrePersist
    @PreUpdate
    public void updateCounts() {
        if (users == null) {
            this.userCount = 0;
        } else {
            this.userCount = this.users.size();
        }
        if (cameras == null) {
            this.cameraCount = 0;
        } else {
            this.cameraCount = this.cameras.size();
        }
    }
}
