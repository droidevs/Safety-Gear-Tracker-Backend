package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Camera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long>, JpaSpecificationExecutor<Camera> {

    List<Camera> findByActiveTrueAndRecordingActiveTrue();

    // Added method to find all active cameras
    List<Camera> findAllByActiveTrue();

    @Query("SELECT c FROM Camera c LEFT JOIN c.zone z LEFT JOIN z.users u GROUP BY c ORDER BY COUNT(u) DESC")
    Page<Camera> findAllOrderByUserCountDesc(Specification<Camera> spec, Pageable pageable);

    @Query("SELECT c FROM Camera c LEFT JOIN c.zone z LEFT JOIN z.users u GROUP BY c ORDER BY COUNT(u) ASC")
    Page<Camera> findAllOrderByUserCountAsc(Specification<Camera> spec, Pageable pageable);

    Optional<Camera> findByName(String name);
}
