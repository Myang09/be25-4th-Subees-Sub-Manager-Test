package com.subees.submanager.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;
    private String service;
    private String serverTime;
}