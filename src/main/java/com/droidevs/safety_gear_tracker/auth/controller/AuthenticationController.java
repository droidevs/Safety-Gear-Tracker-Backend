package com.droidevs.safety_gear_tracker.auth.controller;


import com.droidevs.safety_gear_tracker.auth.dtos.*;
import com.droidevs.safety_gear_tracker.handler.exception.UserNotFoundException;
import com.droidevs.safety_gear_tracker.auth.service.AuthenticationService;
import com.droidevs.safety_gear_tracker.auth.service.DailyCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name= "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;
    private final DailyCodeService dailyCodeService;


    @GetMapping("is_authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(userDetails != null);
    }

    @GetMapping("is_verified")
    public ResponseEntity<Boolean> isVerified(@AuthenticationPrincipal UserDetails userDetails){
        if (userDetails == null || userDetails.getUsername() == null){
            throw new UserNotFoundException();
        }
        return ResponseEntity.ok(service.isUserVerified(userDetails.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    ) throws MessagingException {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        AuthenticationResponse response = service.authenticate(request);
        ResponseCookie cookie = ResponseCookie.from("jwt", response.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }


    @PostMapping("/send-otp")
    public ResponseEntity<?> sendVerifyOtp(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws MessagingException {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        service.sendOtp(userDetails.getUsername());
        return ResponseEntity.ok("Verification code sent");
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyUser(
            @RequestBody @Valid EmailVerificationRequest verifyUserRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws MessagingException {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        service.verifyUser(userDetails.getUsername(), verifyUserRequest.getOtp());
        return ResponseEntity.ok("Account verified successfully");
    }
    
    @PostMapping("/validate-daily-code")
    public ResponseEntity<?> validateDailyCode(
            @RequestBody @Valid DailyCodeValidationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        dailyCodeService.validateCode(userDetails.getUsername(), request.getCode());
        return ResponseEntity.ok("Daily code validated successfully.");
    }


    @PostMapping("/send-reset-otp")
    public void sendResetOtp(
            @RequestBody @Valid String email
    ){

        //TODO
    }

    @PostMapping("/reset-password")
    public void resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ){
        service.resetPassword(request);
    }

    @PostMapping("/reset-password-otp")
    public void resetPassword(
            @RequestBody @Valid ResetPasswordOtpRequest request
    ){
        service.resetPasswordOtp(request);
    }
}
