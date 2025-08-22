package com.droidevs.safety_gear_tracker.dto;

public class ZoneRequestDto {

    private String name;

    public ZoneRequestDto() {
    }

    public ZoneRequestDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
