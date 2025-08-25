package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Page<User> findByZonesId(Long zoneId, Pageable pageable);
    List<User> findByOtpVerifiedFalseAndCreatedAtBefore(LocalDateTime threshold);
}
