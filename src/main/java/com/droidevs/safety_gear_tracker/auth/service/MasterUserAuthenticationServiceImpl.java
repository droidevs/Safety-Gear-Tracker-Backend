
package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MasterUserAuthenticationServiceImpl implements MasterUserAuthenticationService {

    private final UserRepository userRepository;
    private final JwtAuthService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${master.user.email}")
    private String masterEmail;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (!request.email().equals(masterEmail)) {
            throw new IllegalArgumentException("Invalid credentials for master user.");
        }
        
        userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) auth.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());
        
        String jwtToken = jwtService.generateToken(claims, user);

        return new AuthenticationResponse(jwtToken, jwtService.getJwtSecretInfo().getExpirationTime());
    }
}
