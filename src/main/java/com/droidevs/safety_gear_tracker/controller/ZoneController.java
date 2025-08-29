package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.service.ZoneService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zones")
@RequiredArgsConstructor
@Tag(name = "Zones")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping
    public ResponseEntity<ZoneDetailResponseDto> createZone(@Valid @RequestBody AddZoneRequestDto addZoneRequestDto) {
        ZoneDetailResponseDto createdZone = zoneService.createZone(addZoneRequestDto);
        return new ResponseEntity<>(createdZone, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ZonePagingResponseDto> getAllZones(ZonePagingRequestDto zonePagingRequestDto) {
        Page<ZoneSummaryResponseDto> zones = zoneService.getAllZones(zonePagingRequestDto);
        return new ResponseEntity<>(new ZonePagingResponseDto(zones), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneDetailResponseDto> getZoneById(@PathVariable("id") Long id) {
        ZoneDetailResponseDto zone = zoneService.getZoneById(id);
        return new ResponseEntity<>(zone, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneDetailResponseDto> updateZone(@PathVariable("id") Long id, @Valid @RequestBody UpdateZoneRequestDto updateZoneRequestDto) {
        ZoneDetailResponseDto updatedZone = zoneService.updateZone(id, updateZoneRequestDto);
        return new ResponseEntity<>(updatedZone, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable("id") Long id) {
        zoneService.deleteZone(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
