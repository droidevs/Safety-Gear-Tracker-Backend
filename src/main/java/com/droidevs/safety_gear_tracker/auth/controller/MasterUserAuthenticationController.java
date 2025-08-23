package com.droidevs.safety_gear_tracker.auth.controller;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.auth.service.MasterUserAuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/master")
@RequiredArgsConstructor
@Tag(name= "Master Authentication")
public class MasterUserAuthenticationController {

    private final MasterUserAuthenticationService masterUserAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateMaster(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(masterUserAuthenticationService.authenticate(request));
    }
}
