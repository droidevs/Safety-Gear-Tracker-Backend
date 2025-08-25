package com.droidevs.safety_gear_tracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.Data;
import org.hibernate.annotations.Formula;

@Entity
@Data
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String description;

    @ManyToMany(mappedBy = "zones")
    private Set<User> users;
    
    @OneToMany(mappedBy = "zone")
    private Set<Camera> cameras;

    @Formula("(select count(*) from user_zones uz where uz.zone_id = id)")
    private int userCount;

    @Formula("(select count(*) from cameras c where c.zone_id = id)")
    private int cameraCount;
    
    public Zone() {
    }

    public Zone(String name) {
        this.name = name;
    }
}
