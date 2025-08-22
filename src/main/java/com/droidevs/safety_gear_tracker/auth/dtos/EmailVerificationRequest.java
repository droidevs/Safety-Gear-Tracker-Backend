package com.droidevs.safety_gear_tracker.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationRequest {
    @NotBlank(message = "OTP cannot be blank")
    private String otp;
}
