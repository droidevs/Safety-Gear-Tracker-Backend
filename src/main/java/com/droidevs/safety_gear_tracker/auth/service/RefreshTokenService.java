package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.token.RefreshToken;
import com.droidevs.safety_gear_tracker.auth.token.RefreshTokenRepository;
import com.droidevs.safety_gear_tracker.handler.exception.TokenRefreshException;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    // BUG-05 FIX: inject UserRepository so deleteByUserId can resolve the User correctly.
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new sign-in request");
        }
        return token;
    }

    /**
     * BUG-05 FIX: The original implementation called
     * {@code refreshTokenRepository.findById(userId)}, which looks up a
     * {@link RefreshToken} row by its own primary key — not by the owning
     * user's id.  This caused silent no-ops (wrong row) or
     * NoSuchElementException on {@code .get()}.
     *
     * Fix: look the {@link User} up via {@link UserRepository} first, then
     * delegate to the existing {@code deleteByUser} method.
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
}