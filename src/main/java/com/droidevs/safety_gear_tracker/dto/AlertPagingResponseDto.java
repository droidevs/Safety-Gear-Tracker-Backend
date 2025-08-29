package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

public class AlertPagingResponseDto extends PagingResponseDto<AlertSummaryResponseDto> {
    public AlertPagingResponseDto(Page<AlertSummaryResponseDto> page) {
        super(page);
    }
}
