package com.droidevs.safety_gear_tracker.controller;

import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertSummaryResponseDto;
import com.droidevs.safety_gear_tracker.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertController).build();
    }

    @Test
    void getAllAlerts_shouldReturnAlerts() throws Exception {
        Page<AlertSummaryResponseDto> alertPage = new PageImpl<>(Collections.emptyList());
        when(alertService.getAllAlerts(0, 10)).thenReturn(alertPage);

        mockMvc.perform(get("/alerts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAlertById_shouldReturnAlert() throws Exception {
        Long alertId = 1L;
        AlertResponseDto alertResponseDto = new AlertResponseDto(alertId, "Test Camera", "Test Alert", LocalDateTime.now(), "screenshot.jpg", 1L);
        when(alertService.getAlertById(alertId)).thenReturn(Optional.of(alertResponseDto));

        mockMvc.perform(get("/alerts/{alertId}", alertId))
                .andExpect(status().isOk());
    }

    @Test
    void getAlertScreenshot_shouldReturnScreenshot() throws Exception {
        Long alertId = 1L;
        byte[] screenshot = "screenshot".getBytes();
        when(alertService.getAlertScreenshot(alertId)).thenReturn(screenshot);

        mockMvc.perform(get("/alerts/{alertId}/screenshot", alertId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void streamAlertScreenshot_shouldStreamScreenshot() throws Exception {
        Long alertId = 1L;
        InputStreamResource stream = new InputStreamResource(new ByteArrayInputStream("screenshot".getBytes()));
        when(alertService.getAlertScreenshotStream(alertId)).thenReturn(stream);

        mockMvc.perform(get("/alerts/{alertId}/screenshot/stream", alertId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void streamAlertRecording_shouldStreamRecording() throws Exception {
        Long alertId = 1L;
        InputStreamResource stream = new InputStreamResource(new ByteArrayInputStream("recording".getBytes()));
        when(alertService.getAlertRecordingStream(alertId)).thenReturn(stream);

        mockMvc.perform(get("/alerts/{alertId}/recording/stream", alertId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType(MediaType.parseMediaType("video/mp4")));
    }
}
