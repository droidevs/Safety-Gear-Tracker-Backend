package com.droidevs.safety_gear_tracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonePagingRequestDto extends PagingRequestDto {
    private String search;
    private ZoneSortCriteria sortBy = ZoneSortCriteria.NO_ORDER;
}
