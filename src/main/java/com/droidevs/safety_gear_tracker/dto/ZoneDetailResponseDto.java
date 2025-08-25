package com.droidevs.safety_gear_tracker.dto;

public record ZoneDetailResponseDto(
    Long id,
    String name,
    String description,
    long userCount
) {
}
