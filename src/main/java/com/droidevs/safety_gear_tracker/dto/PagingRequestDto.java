
package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
public class PagingRequestDto {
    @JsonProperty("page_number")
    private int pageNumber = 0;
    @JsonProperty("page_size")
    private int pageSize = 10;

    public Pageable toPageable() {
        return PageRequest.of(pageNumber, pageSize);
    }
}
