package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    List<Camera> findByActiveTrueAndIsRecordingActiveTrue();
    void deleteById(Long id);
    Optional<Camera> findByName(String name);
}
