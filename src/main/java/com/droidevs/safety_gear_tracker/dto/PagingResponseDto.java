
package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagingResponseDto<T> {
    @JsonProperty("content")
    private List<T> content;
    @JsonProperty("page_number")
    private int pageNumber;
    @JsonProperty("page_size")
    private int pageSize;
    @JsonProperty("total_elements")
    private long totalElements;
    @JsonProperty("total_pages")
    private int totalPages;
    @JsonProperty("last")
    private boolean last;

    public PagingResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}
