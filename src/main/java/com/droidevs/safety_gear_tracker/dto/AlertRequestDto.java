package com.droidevs.safety_gear_tracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class AlertRequestDto {
    private String cameraName;
    private String description;
    private MultipartFile screenshot;
}
