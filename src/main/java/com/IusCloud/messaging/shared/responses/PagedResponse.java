package com.IusCloud.messaging.shared.responses;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PagedResponse<T> {
    private LocalDateTime timestamp;
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PagedResponse(Page<T> page) {
        this.timestamp = LocalDateTime.now();
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}