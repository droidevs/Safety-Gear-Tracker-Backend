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
public class DailyCodeValidationRequest {
    @NotBlank(message = "Code cannot be blank")
    private String code;
}
