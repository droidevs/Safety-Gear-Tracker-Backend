package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

public class ZonePagingResponseDto extends PagingResponseDto<ZoneSummaryResponseDto> {
    public ZonePagingResponseDto(Page<ZoneSummaryResponseDto> page) {
        super(page);
    }
}
