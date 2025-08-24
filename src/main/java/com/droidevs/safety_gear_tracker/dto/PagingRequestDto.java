
package com.droidevs.safety_gear_tracker.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
public class PagingRequestDto {
    private int pageNumber = 0;
    private int pageSize = 10;

    public Pageable toPageable() {
        return PageRequest.of(pageNumber, pageSize);
    }
}
