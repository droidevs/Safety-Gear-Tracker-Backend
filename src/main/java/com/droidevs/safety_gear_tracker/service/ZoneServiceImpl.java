
package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.mappers.ZoneMapper;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import com.droidevs.safety_gear_tracker.repository.ZoneSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    @Override
    public ZoneDetailResponseDto createZone(AddZoneRequestDto addZoneRequestDto) {
        Zone zone = new Zone();
        zone.setName(addZoneRequestDto.name());
        zone.setDescription(addZoneRequestDto.description());
        Zone savedZone = zoneRepository.save(zone);
        return zoneMapper.toDetailDto(savedZone);
    }

    @Override
    public Page<ZoneSummaryResponseDto> getAllZones(ZonePagingRequestDto zonePagingRequestDto) {
        Sort sort = switch (zonePagingRequestDto.getSortBy()) {
            case MORE_MANAGERS -> Sort.by(Sort.Direction.DESC, "userCount");
            case LESS_MANAGERS -> Sort.by(Sort.Direction.ASC, "userCount");
            case MORE_CAMERAS -> Sort.by(Sort.Direction.DESC, "cameraCount");
            case LESS_CAMERAS -> Sort.by(Sort.Direction.ASC, "cameraCount");
            default -> Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(zonePagingRequestDto.getPageNumber(), zonePagingRequestDto.getPageSize(), sort);
        return zoneRepository.findAll(ZoneSpecification.search(zonePagingRequestDto.getSearch()), pageable)
                .map(zoneMapper::toSummaryDto);
    }

    @Override
    public ZoneDetailResponseDto getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: "  + id));
        return zoneMapper.toDetailDto(zone);
    }

    @Override
    public ZoneDetailResponseDto updateZone(Long id, UpdateZoneRequestDto updateZoneRequestDto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        zone.setName(updateZoneRequestDto.name());
        zone.setDescription(updateZoneRequestDto.description());
        Zone updatedZone = zoneRepository.save(zone);
        return zoneMapper.toDetailDto(updatedZone);
    }

    @Override
    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }
}
