package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.*;
import org.springframework.data.domain.Page;

public interface ZoneService {
    ZoneDetailResponseDto createZone(AddZoneRequestDto addZoneRequestDto);
    Page<ZoneSummaryResponseDto> getAllZones(ZonePagingRequestDto zonePagingRequestDto);
    ZoneDetailResponseDto getZoneById(Long id);
    ZoneDetailResponseDto updateZone(Long id, UpdateZoneRequestDto updateZoneRequestDto);
    void deleteZone(Long id);
}
