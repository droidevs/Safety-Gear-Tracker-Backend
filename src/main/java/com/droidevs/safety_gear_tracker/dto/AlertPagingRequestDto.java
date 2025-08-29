package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertPagingRequestDto extends PagingRequestDto {
    // No additional fields needed for now, but can be added later for filtering/sorting
}
