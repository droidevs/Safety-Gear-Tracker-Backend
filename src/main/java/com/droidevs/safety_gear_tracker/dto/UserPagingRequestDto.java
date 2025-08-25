package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPagingRequestDto extends PagingRequestDto {

    @JsonProperty("zone")
    private Long zoneId;

    @JsonProperty("sort_by")
    private UserResponsibilitySort sortBy;

    @Override
    public Pageable toPageable() {
        if (sortBy != null) {
            Sort sort = switch (sortBy) {
                case MORE_RESPONSIBLE -> Sort.by("zoneCount").descending();
                case LESS_RESPONSIBLE -> Sort.by("zoneCount").ascending();
            };
            return PageRequest.of(getPageNumber(), getPageSize(), sort);
        } else {
            return super.toPageable();
        }
    }
}
