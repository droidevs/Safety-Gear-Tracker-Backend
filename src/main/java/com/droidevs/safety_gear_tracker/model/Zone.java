package com.droidevs.safety_gear_tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Collections;
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

    // BUG-15 FIX (part 1): change fetch strategy to EAGER so the collections
    // are always initialised when @PrePersist / @PreUpdate fires.
    // LAZY collections accessed outside an active Hibernate session (e.g. during
    // JPA lifecycle callbacks invoked by Spring Data) throw
    // LazyInitializationException.  Making them EAGER eliminates that risk.
    // For very large deployments consider a dedicated count column updated via
    // service-layer logic instead, but EAGER is the safest minimal fix here.
    @ManyToMany(mappedBy = "zones", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "zone", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Camera> cameras;

    /**
     * BUG-15 FIX (part 2): guard every collection access with a null check and
     * use {@link Collections#emptySet()} as fallback so the callback can never
     * throw NPE or LazyInitializationException regardless of context.
     */
    @PrePersist
    @PreUpdate
    private void updateCounts() {
        this.userCount   = (users   != null) ? users.size()   : 0;
        this.cameraCount = (cameras != null) ? cameras.size() : 0;
    }

    // ── identity based on id ─────────────────────────────────────────────────

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