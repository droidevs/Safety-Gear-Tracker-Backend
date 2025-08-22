package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.ZoneRequestDto;
import com.droidevs.safety_gear_tracker.dto.ZoneResponseDto;
import com.droidevs.safety_gear_tracker.service.ZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping
    public ResponseEntity<ZoneResponseDto> createZone(@RequestBody ZoneRequestDto zoneRequestDto) {
        ZoneResponseDto createdZone = zoneService.createZone(zoneRequestDto);
        return new ResponseEntity<>(createdZone, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponseDto>> getAllZones() {
        List<ZoneResponseDto> zones = zoneService.getAllZones();
        return new ResponseEntity<>(zones, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponseDto> getZoneById(@PathVariable Long id) {
        ZoneResponseDto zone = zoneService.getZoneById(id);
        return new ResponseEntity<>(zone, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneResponseDto> updateZone(@PathVariable Long id, @RequestBody ZoneRequestDto zoneRequestDto) {
        ZoneResponseDto updatedZone = zoneService.updateZone(id, zoneRequestDto);
        return new ResponseEntity<>(updatedZone, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
