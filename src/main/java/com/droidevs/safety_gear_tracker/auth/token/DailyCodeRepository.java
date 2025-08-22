package com.droidevs.safety_gear_tracker.auth.token;

import com.droidevs.safety_gear_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCodeRepository extends JpaRepository<DailyCode, Long> {
    Optional<DailyCode> findByUserAndCode(User user, String code);
    List<DailyCode> findByUser(User user);
    Optional<DailyCode> findTopByUserOrderByCreatedAtDesc(User user);
}
