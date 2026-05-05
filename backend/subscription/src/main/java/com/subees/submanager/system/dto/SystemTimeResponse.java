package com.subees.submanager.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SystemTimeResponse {
    private String serverTime;
    private String zoneId;
    private long epochMillis;
}