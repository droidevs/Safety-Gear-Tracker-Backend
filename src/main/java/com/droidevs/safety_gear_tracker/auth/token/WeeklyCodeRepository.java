package com.droidevs.safety_gear_tracker.auth.token;

import com.droidevs.safety_gear_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyCodeRepository extends JpaRepository<WeeklyCode, Long> {
    Optional<WeeklyCode> findByUserAndCode(User user, String code);
    List<WeeklyCode> findByUser(User user);
    Optional<WeeklyCode> findTopByUserOrderByCreatedAtDesc(User user);
}
