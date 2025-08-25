package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.ZoneDetailResponseDto;
import com.droidevs.safety_gear_tracker.dto.ZoneSummaryResponseDto;
import com.droidevs.safety_gear_tracker.model.Zone;
import org.springframework.stereotype.Component;

@Component
public class ZoneMapper {

    public ZoneDetailResponseDto toDetailDto(Zone zone) {
        return new ZoneDetailResponseDto(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.getUsers() != null ? zone.getUsers().size() : 0
        );
    }

    public ZoneSummaryResponseDto toSummaryDto(Zone zone) {
        return new ZoneSummaryResponseDto(
                zone.getId(),
                zone.getName()
        );
    }
}
