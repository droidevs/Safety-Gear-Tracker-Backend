package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
