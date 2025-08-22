package com.droidevs.safety_gear_tracker.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;
}
