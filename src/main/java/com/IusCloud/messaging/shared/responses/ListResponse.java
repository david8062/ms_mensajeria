package com.IusCloud.messaging.shared.responses;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ListResponse<T> {
    private LocalDateTime timestamp;
    private int count;
    private List<T> data;

    public ListResponse(List<T> data) {
        this.timestamp = LocalDateTime.now();
        this.count = data.size();
        this.data = data;
    }
}