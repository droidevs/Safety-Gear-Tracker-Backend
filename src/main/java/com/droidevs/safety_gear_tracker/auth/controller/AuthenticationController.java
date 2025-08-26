
package com.droidevs.safety_gear_tracker.auth.controller;


import com.droidevs.safety_gear_tracker.auth.dtos.*;
import com.droidevs.safety_gear_tracker.handler.exception.UserNotFoundException;
import com.droidevs.safety_gear_tracker.auth.service.AuthenticationService;
import com.droidevs.safety_gear_tracker.auth.service.WeeklyCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name= "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;
    private final WeeklyCodeService weeklyCodeService;


    @GetMapping("is_authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@AuthenticationPrincipal String email){
        return ResponseEntity.ok(email != null);
    }

    @GetMapping("is_verified")
    public ResponseEntity<Boolean> isVerified(@AuthenticationPrincipal String email){
        if (email == null){
            throw new UserNotFoundException();
        }
        return ResponseEntity.ok(service.isUserVerified(email));
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
        ResponseCookie cookie = ResponseCookie.from("jwt", response.token())
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
            @AuthenticationPrincipal String email
    ) throws MessagingException {
        if (email == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        service.sendOtp(email);
        return ResponseEntity.ok("Verification code sent");
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyUser(
            @RequestBody @Valid EmailVerificationRequest verifyUserRequest,
            @AuthenticationPrincipal String email
    ) throws MessagingException {
        if (email == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        service.verifyUser(email, verifyUserRequest.otp());
        return ResponseEntity.ok("Account verified successfully");
    }
    
    @PostMapping("/validate-weekly-code")
    public ResponseEntity<?> validateWeeklyCode(
            @RequestBody @Valid DailyCodeValidationRequest request,
            @AuthenticationPrincipal String email
    ) {
        if (email == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        weeklyCodeService.validateCode(email, request.code());
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
            @RequestBody @Valid ResetPasswordRequest request,
            @AuthenticationPrincipal String email
    ){
        service.resetPassword(request, email);
    }

    @PostMapping("/reset-password-otp")
    public void resetPassword(
            @RequestBody @Valid ResetPasswordOtpRequest request
    ){
        service.resetPasswordOtp(request);
    }
}
