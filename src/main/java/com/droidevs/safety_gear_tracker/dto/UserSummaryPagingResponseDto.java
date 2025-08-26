package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class UserSummaryPagingResponseDto extends PagingResponseDto<UserSummaryResponseDto> {
    public UserSummaryPagingResponseDto(Page<UserSummaryResponseDto> page) {
        super(page);
    }
}
