package com.droidevs.safety_gear_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectedObject {
    private String label;
    private BoundingBox boundingBox;
}
