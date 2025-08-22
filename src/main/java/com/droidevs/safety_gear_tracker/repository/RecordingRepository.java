package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Recording;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

    @Query("SELECT r FROM Recording r WHERE r.camera.id = :cameraId AND r.timestamp <= :timestamp ORDER BY r.timestamp DESC")
    Optional<Recording> findLastRecordingBeforeTimestamp(@Param("cameraId") Long cameraId, @Param("timestamp") LocalDateTime timestamp);

    Page<Recording> findByCameraIdIn(List<Long> cameraIds, Pageable pageable);
}
