package com.droidevs.safety_gear_tracker.config;

import com.droidevs.safety_gear_tracker.auth.security.CustomAuthenticationEntryPoint;
import com.droidevs.safety_gear_tracker.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // BUG-14 FIX: WebConfig adds the "/api/v1" prefix to every @RestController,
    // so all camera / zone / alert / user endpoints are actually served under
    // /api/v1/cameras, /api/v1/zones, etc.
    // The previous config only whitelisted /api/v1/auth/** and left
    // /api/v1/cameras/** etc. requiring authentication — which is correct —
    // but the JWT filter also needs to intercept /api/v1/** (not just the
    // root-level paths that appear in the controller @RequestMapping values).
    // Spring Security matches the *actual* request URI, which already carries
    // the prefix by the time it reaches the filter chain, so the rules below
    // simply reference the full prefixed paths consistently.
    public static final String API_PREFIX = "/api/v1";

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(
                                // Auth endpoints — public
                                API_PREFIX + "/auth/**",
                                // Swagger UI
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                // Thymeleaf UI pages & static assets
                                "/ui/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        // BUG-14 FIX: every other /api/v1/** route requires authentication.
                        // Because WebConfig prefixes all @RestControllers with /api/v1,
                        // the rule below covers /api/v1/cameras/**, /api/v1/zones/**,
                        // /api/v1/users/**, /api/v1/alerts/**, /api/v1/recordings/**,
                        // /api/v1/stream/**, etc. — no separate per-resource rules needed.
                        .requestMatchers(API_PREFIX + "/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}