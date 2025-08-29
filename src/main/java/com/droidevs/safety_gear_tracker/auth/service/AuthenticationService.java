
package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;
import com.droidevs.safety_gear_tracker.auth.dtos.ForgotPasswordRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RefreshTokenRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.RegisterRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordOtpRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.ResetPasswordRequest;
import jakarta.mail.MessagingException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request) throws MessagingException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    void sendOtp(String email) throws MessagingException;
    void verifyUser(String email, String otp);
    boolean isUserVerified(String email);
    void changePassword(ResetPasswordRequest request, String email);
    void resetPasswordOtp(ResetPasswordOtpRequest request);
    void forgotPassword(ForgotPasswordRequest request) throws MessagingException;
    void logout(String email);
}
