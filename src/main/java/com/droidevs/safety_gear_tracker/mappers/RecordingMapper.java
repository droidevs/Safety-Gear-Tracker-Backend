package com.droidevs.safety_gear_tracker.mappers;

import com.droidevs.safety_gear_tracker.dto.RecordingResponseDto;
import com.droidevs.safety_gear_tracker.model.Recording;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordingMapper {
    @Mapping(source = "camera.name", target = "cameraName")
    RecordingResponseDto toDto(Recording recording);
}
