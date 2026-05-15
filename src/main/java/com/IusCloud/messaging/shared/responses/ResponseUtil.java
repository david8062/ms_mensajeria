package com.IusCloud.messaging.shared.responses;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Operación exitosa", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Recurso creado exitosamente", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> custom(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), message, data));
    }


    public static <T> ResponseEntity<ListResponse<T>> list(List<T> data) {
        return ResponseEntity.ok(new ListResponse<>(data));
    }


    public static <T> ResponseEntity<PagedResponse<T>> paged(Page<T> page) {
        return ResponseEntity.ok(new PagedResponse<>(page));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

}