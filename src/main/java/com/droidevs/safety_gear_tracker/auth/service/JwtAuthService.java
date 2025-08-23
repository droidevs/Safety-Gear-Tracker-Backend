
package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.config.JwtSecretInfo;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtAuthService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateToken(UserDetails userDetails);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    Date extractExpiration(String token);
    Claims extractAllClaims(String token);
    Key getSignInKey();
    JwtSecretInfo getJwtSecretInfo();
}
