
package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.ZoneRequestDto;
import com.droidevs.safety_gear_tracker.dto.ZoneResponseDto;
import com.droidevs.safety_gear_tracker.mappers.ZoneMapper;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.model.Zone;
import com.droidevs.safety_gear_tracker.repository.UserRepository;
import com.droidevs.safety_gear_tracker.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final ZoneMapper zoneMapper;

    @Override
    public ZoneResponseDto createZone(ZoneRequestDto zoneRequestDto) {
        Zone zone = new Zone();
        zone.setName(zoneRequestDto.name());
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
        zone.setName(zoneRequestDto.name());
        Zone updatedZone = zoneRepository.save(zone);
        return zoneMapper.toDto(updatedZone);
    }

    @Override
    public void deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));

        List<User> users = userRepository.findByZonesId(id, null).getContent();
        for (User user : users) {
            user.getZones().remove(zone);
            user.updateZoneCount();
            userRepository.save(user);
        }

        zoneRepository.deleteById(id);
    }
}
