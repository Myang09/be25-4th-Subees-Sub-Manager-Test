package com.subees.submanager.common.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int errorcode;
    private String status;
    private String message;
}