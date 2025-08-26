package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

public class CameraSummaryPagingResponseDto extends PagingResponseDto<CameraSummaryDto> {
    public CameraSummaryPagingResponseDto(Page<CameraSummaryDto> page) {
        super(page);
    }
}
