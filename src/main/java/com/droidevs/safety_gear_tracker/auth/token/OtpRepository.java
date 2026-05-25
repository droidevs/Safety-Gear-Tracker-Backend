package com.droidevs.safety_gear_tracker.auth.token;

import com.droidevs.safety_gear_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BUG-16 FIX: findByUser returned Optional<Otp> but a user can accumulate
 * multiple OTPs. If > 1 row exists, Spring throws IncorrectResultSizeDataAccessException.
 * Fix: return List<Otp> so all old OTPs can be deleted before issuing a new one.
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByUserAndOtp(User user, String otp);

    // Changed from Optional<Otp> to List<Otp>
    List<Otp> findByUser(User user);
}
