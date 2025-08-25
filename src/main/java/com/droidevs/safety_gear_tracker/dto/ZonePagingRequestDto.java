package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonePagingRequestDto extends PagingRequestDto {
    @JsonProperty("search")
    private String search;
    @JsonProperty("sort_by")
    private ZoneSortCriteria sortBy = ZoneSortCriteria.NO_ORDER;
}
