package com.subees.submanager.common.model.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class BaseResponseDto<T> {
    private final int code;
    private final String message;
    private final T data;

    public BaseResponseDto(HttpStatus httpStatus, T data) {
        this.code = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.data = data;
    }

    public BaseResponseDto(HttpStatus status, String message) {
        this.code = status.value();
        this.message = message;
        this.data = null;
    }

    public BaseResponseDto(HttpStatus httpStatus, String message, T data) {
        this.code = httpStatus.value();
        this.message = message;
        this.data = data;
    }
}