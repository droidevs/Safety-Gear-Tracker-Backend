package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.ZoneResponseDto;
import com.droidevs.safety_gear_tracker.model.Zone;
import org.springframework.stereotype.Component;

@Component
public class ZoneMapper {

    public ZoneResponseDto toDto(Zone zone) {
        return new ZoneResponseDto(zone.getId(), zone.getName());
    }
}
