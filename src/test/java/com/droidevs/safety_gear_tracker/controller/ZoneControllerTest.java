package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.service.ZoneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ZoneControllerTest {

    @Mock
    private ZoneService zoneService;

    @InjectMocks
    private ZoneController zoneController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(zoneController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createZone_shouldCreateZone() throws Exception {
        AddZoneRequestDto requestDto = new AddZoneRequestDto("Test Zone", "Description");
        ZoneDetailResponseDto responseDto = new ZoneDetailResponseDto(1L, "Test Zone", "Description", 0L);
        when(zoneService.createZone(any(AddZoneRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllZones_shouldReturnZones() throws Exception {
        Page<ZoneSummaryResponseDto> zonePage = new PageImpl<>(Collections.emptyList());
        when(zoneService.getAllZones(any(ZonePagingRequestDto.class))).thenReturn(zonePage);

        mockMvc.perform(get("/zones"))
                .andExpect(status().isOk());
    }

    @Test
    void getZoneById_shouldReturnZone() throws Exception {
        Long zoneId = 1L;
        ZoneDetailResponseDto responseDto = new ZoneDetailResponseDto(zoneId, "Test Zone", "Description",  0L);
        when(zoneService.getZoneById(zoneId)).thenReturn(responseDto);

        mockMvc.perform(get("/zones/{id}", zoneId))
                .andExpect(status().isOk());
    }

    @Test
    void updateZone_shouldUpdateZone() throws Exception {
        Long zoneId = 1L;
        UpdateZoneRequestDto requestDto = new UpdateZoneRequestDto("New Name", "New Description");
        ZoneDetailResponseDto responseDto = new ZoneDetailResponseDto(zoneId, "New Name", "New Description", 0L);
        when(zoneService.updateZone(eq(zoneId), any(UpdateZoneRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/zones/{id}", zoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteZone_shouldDeleteZone() throws Exception {
        Long zoneId = 1L;
        doNothing().when(zoneService).deleteZone(zoneId);

        mockMvc.perform(delete("/zones/{id}", zoneId))
                .andExpect(status().isNoContent());
    }
}