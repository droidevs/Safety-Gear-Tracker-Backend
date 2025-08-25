
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
    @JsonProperty("zone_id")
    private Long zoneId;
    @JsonProperty("responsibility_sort")
    private UserResponsibilitySort responsibilitySort;

    @Override
    public Pageable toPageable() {
        if (responsibilitySort != null) {
            Sort.Direction direction = responsibilitySort == UserResponsibilitySort.MORE_RESPONSIBLE ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(getPageNumber(), getPageSize(), Sort.by(direction, "zoneCount"));
        }
        return super.toPageable();
    }
}
