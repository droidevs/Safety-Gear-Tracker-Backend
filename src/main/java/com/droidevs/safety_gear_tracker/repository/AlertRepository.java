package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByCameraIdIn(List<Long> cameraIds, Pageable pageable);
}
