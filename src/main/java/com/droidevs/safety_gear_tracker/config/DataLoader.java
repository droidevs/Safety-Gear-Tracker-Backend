package com.droidevs.safety_gear_tracker.config;

import com.droidevs.safety_gear_tracker.model.Role;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.RoleRepository;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${master.user.email}")
    private String masterEmail;
    @Value("${master.user.password}")
    private String masterPassword;
    @Value("${master.user.firstname}")
    private String masterFirstname;
    @Value("${master.user.lastname}")
    private String masterLastname;

    @Override
    public void run(String... args) throws Exception {
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));
        Role masterRole = roleRepository.findByName("MASTER").orElseGet(() -> roleRepository.save(Role.builder().name("MASTER").build()));

        if (userRepository.findByEmail(masterEmail).isEmpty()) {
            User masterUser = User.builder()
                    .email(masterEmail)
                    .password(passwordEncoder.encode(masterPassword))
                    .firstname(masterFirstname)
                    .lastname(masterLastname)
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of(masterRole))
                    .build();
            userRepository.save(masterUser);
        }
    }
}
