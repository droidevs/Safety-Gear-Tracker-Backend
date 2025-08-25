package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {
}
