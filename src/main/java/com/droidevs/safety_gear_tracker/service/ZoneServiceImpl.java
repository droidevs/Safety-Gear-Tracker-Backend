package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.ZoneRequestDto;
import com.droidevs.safety_gear_tracker.dto.ZoneResponseDto;
import com.droidevs.safety_gear_tracker.mappers.ZoneMapper;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    public ZoneServiceImpl(ZoneRepository zoneRepository, ZoneMapper zoneMapper) {
        this.zoneRepository = zoneRepository;
        this.zoneMapper = zoneMapper;
    }

    @Override
    public ZoneResponseDto createZone(ZoneRequestDto zoneRequestDto) {
        Zone zone = new Zone();
        zone.setName(zoneRequestDto.getName());
        Zone savedZone = zoneRepository.save(zone);
        return zoneMapper.toDto(savedZone);
    }

    @Override
    public List<ZoneResponseDto> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(zoneMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ZoneResponseDto getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));
        return zoneMapper.toDto(zone);
    }

    @Override
    public ZoneResponseDto updateZone(Long id, ZoneRequestDto zoneRequestDto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));
        zone.setName(zoneRequestDto.getName());
        Zone updatedZone = zoneRepository.save(zone);
        return zoneMapper.toDto(updatedZone);
    }

    @Override
    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }
}
