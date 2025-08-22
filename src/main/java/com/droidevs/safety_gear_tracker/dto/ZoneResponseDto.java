package com.droidevs.safety_gear_tracker.dto;

public class ZoneResponseDto {

    private Long id;
    private String name;

    public ZoneResponseDto() {
    }

    public ZoneResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
