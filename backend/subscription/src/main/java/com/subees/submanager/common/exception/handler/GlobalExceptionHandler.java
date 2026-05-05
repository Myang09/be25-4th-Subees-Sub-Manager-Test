package com.subees.submanager.common.exception.handler;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.dto.ApiErrorResponseDto;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UniversityException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(UniversityException e) {
        log.error("UniversityException : {}", e.getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        e.getHttpStatus().value(),
                        e.getStatus(),
                        e.getMessage()
                ),
                e.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(MethodArgumentNotValidException e) {
        StringBuilder errors = new StringBuilder();
        log.error("MethodArgumentNotValidException : {}", e.getMessage());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.append(fieldError.getField())
                    .append("(")
                    .append(fieldError.getDefaultMessage())
                    .append("), ");
        }

        if (errors.length() > 2) {
            errors.replace(errors.lastIndexOf(","), errors.length(), "");
        }

        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        errors.toString()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException : {}", e.getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        ExceptionMessage.INVALID_REQUEST.getMessage()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(IllegalArgumentException e) {
        log.error("IllegalArgumentException : {}", e.getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        e.getMessage()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(IllegalStateException e) {
        log.error("IllegalStateException : {}", e.getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        e.getMessage()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(ResponseStatusException e) {
        String message = e.getReason() != null ? e.getReason() : e.getStatusCode().toString();
        return ResponseEntity.status(e.getStatusCode())
                .body(new ApiErrorResponseDto(
                        e.getStatusCode().value(),
                        e.getStatusCode().toString(),
                        message
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(MaxUploadSizeExceededException e) {
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        "파일 크기는 5MB를 초과할 수 없습니다."
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception e) {
        log.error("Global Exception", e);
        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        ExceptionMessage.INTERNAL_SERVER_ERROR.getHttpStatus().value(),
                        ExceptionMessage.INTERNAL_SERVER_ERROR.name(),
                        ExceptionMessage.INTERNAL_SERVER_ERROR.getMessage()
                ),
                ExceptionMessage.INTERNAL_SERVER_ERROR.getHttpStatus()
        );
    }
}
