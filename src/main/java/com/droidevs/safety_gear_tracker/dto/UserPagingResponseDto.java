
package com.droidevs.safety_gear_tracker.dto;

import org.springframework.data.domain.Page;

public class UserPagingResponseDto extends PagingResponseDto<UserProfileDto> {
    public UserPagingResponseDto(Page<UserProfileDto> page) {
        super(page);
    }
}
