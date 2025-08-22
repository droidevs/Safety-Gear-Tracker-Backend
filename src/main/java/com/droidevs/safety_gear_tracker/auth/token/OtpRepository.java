package com.droidevs.safety_gear_tracker.auth.token;

import com.droidevs.safety_gear_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByOtp(String token);
    Optional<Otp> findByUserAndOtp(User user, String otp);
}
