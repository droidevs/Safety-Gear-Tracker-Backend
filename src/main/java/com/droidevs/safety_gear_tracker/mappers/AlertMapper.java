package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.AlertResponseDto;
import com.droidevs.safety_gear_tracker.dto.AlertSummaryResponseDto;
import com.droidevs.safety_gear_tracker.model.Alert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlertMapper {
    @Mapping(source = "camera.name", target = "cameraName")
    @Mapping(source = "recording.id", target = "recordingId")
    AlertResponseDto toDto(Alert alert);

    @Mapping(source = "camera.name", target = "cameraName")
    AlertSummaryResponseDto toSummaryDto(Alert alert);
}
