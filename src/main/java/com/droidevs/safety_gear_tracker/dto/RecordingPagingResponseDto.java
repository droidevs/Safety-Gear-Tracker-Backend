package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

public class RecordingPagingResponseDto extends PagingResponseDto<RecordingResponseDto> {
    public RecordingPagingResponseDto(Page<RecordingResponseDto> page) {
        super(page);
    }
}
