package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.*;
import com.droidevs.safety_gear_tracker.model.SafetyGearType;
import com.droidevs.safety_gear_tracker.service.CameraService;
import com.droidevs.safety_gear_tracker.service.VideoProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CameraControllerTest {

    @Mock
    private CameraService cameraService;

    @Mock
    private VideoProcessingService videoProcessingService;

    @InjectMocks
    private CameraController cameraController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cameraController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addCamera_shouldAddCamera() throws Exception {
        AddCameraRequestDto requestDto = new AddCameraRequestDto("Test Camera", "192.168.1.1", 8080, "admin", "password", "rtsp://test.com/stream", Set.of(SafetyGearType.HARD_HAT), 1L);
        CameraResponseDto responseDto = new CameraResponseDto(1L, "Test Camera", "192.168.1.1", 8080, true, false, 1L, Collections.emptyList());
        when(cameraService.addCamera(any(AddCameraRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/cameras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllCameras_shouldReturnCameras() throws Exception {
        CameraSummaryPagingResponseDto responseDto = new CameraSummaryPagingResponseDto(new PageImpl<>(Collections.emptyList()));
        when(cameraService.getAllCameras(any(CameraPagingRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(get("/cameras"))
                .andExpect(status().isOk());
    }

    @Test
    void getCameraById_shouldReturnCamera() throws Exception {
        Long cameraId = 1L;
        CameraFullResponseDto responseDto = new CameraFullResponseDto(cameraId, "Test Camera", "192.168.1.1", 8080, "admin", "password", true, false, 1L, Collections.emptyList());
        doReturn(Optional.of(responseDto)).when(cameraService).getCameraById(cameraId);

        mockMvc.perform(get("/cameras/{id}", cameraId))
                .andExpect(status().isOk());
    }

    @Test
    void updateCamera_shouldUpdateCamera() throws Exception {
        Long cameraId = 1L;
        UpdateCameraRequestDto requestDto = new UpdateCameraRequestDto("New Name", "192.168.1.2", 8081, "admin", "newpassword", "rtsp://new.com/stream", Set.of(SafetyGearType.HARD_HAT), 2L, true, false);
        CameraResponseDto responseDto = new CameraResponseDto(cameraId, "New Name", "192.168.1.2", 8081, true, false, 2L, Collections.emptyList());
        when(cameraService.updateCamera(eq(cameraId), any(UpdateCameraRequestDto.class))).thenReturn(Optional.of(responseDto));

        mockMvc.perform(put("/cameras/{id}", cameraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCamera_shouldDeleteCamera() throws Exception {
        Long cameraId = 1L;
        doNothing().when(cameraService).deleteCamera(cameraId);

        mockMvc.perform(delete("/cameras/{id}", cameraId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCameraFeed_shouldReturnFeed() throws Exception {
        Long cameraId = 1L;
        Flux<byte[]> feed = Flux.just("feed".getBytes());
        when(videoProcessingService.getCameraFeed(cameraId)).thenReturn(feed);

        mockMvc.perform(get("/cameras/{id}/feed", cameraId))
                .andExpect(status().isOk());
    }
}
