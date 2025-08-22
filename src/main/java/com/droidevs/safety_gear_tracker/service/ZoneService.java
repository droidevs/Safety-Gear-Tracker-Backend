package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.ZoneRequestDto;
import com.droidevs.safety_gear_tracker.dto.ZoneResponseDto;
import java.util.List;

public interface ZoneService {
    ZoneResponseDto createZone(ZoneRequestDto zoneRequestDto);
    List<ZoneResponseDto> getAllZones();
    ZoneResponseDto getZoneById(Long id);
    ZoneResponseDto updateZone(Long id, ZoneRequestDto zoneRequestDto);
    void deleteZone(Long id);
}
