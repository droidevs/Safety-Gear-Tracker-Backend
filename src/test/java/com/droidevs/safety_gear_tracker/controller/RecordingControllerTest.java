package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.RecordingResponseDto;
import com.droidevs.safety_gear_tracker.service.RecordingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecordingControllerTest {

    @Mock
    private RecordingService recordingService;

    @InjectMocks
    private RecordingController recordingController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recordingController).build();
    }

    @Test
    void getRecordings_shouldReturnRecordings() throws Exception {
        Page<RecordingResponseDto> recordingPage = new PageImpl<>(Collections.emptyList());
        when(recordingService.getAllRecordings(anyInt(), anyInt())).thenReturn(recordingPage);

        mockMvc.perform(get("/recordings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecording_shouldReturnRecording() throws Exception {
        Long recordingId = 1L;
        RecordingResponseDto recording = new RecordingResponseDto(recordingId, "camera", "url", LocalDateTime.now());
        when(recordingService.getRecordingById(recordingId)).thenReturn(Optional.of(recording));

        mockMvc.perform(get("/recordings/{recordingId}", recordingId))
                .andExpect(status().isOk());
    }

    @Test
    void getRecording_shouldReturnNotFound() throws Exception {
        Long recordingId = 1L;
        when(recordingService.getRecordingById(recordingId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/recordings/{recordingId}", recordingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRecording_shouldDeleteRecording() throws Exception {
        Long recordingId = 1L;
        doNothing().when(recordingService).deleteRecording(recordingId);

        mockMvc.perform(delete("/recordings/{recordingId}", recordingId))
                .andExpect(status().isNoContent());
    }
}
