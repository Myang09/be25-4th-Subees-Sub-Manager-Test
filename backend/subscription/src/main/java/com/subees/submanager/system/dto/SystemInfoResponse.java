package com.subees.submanager.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SystemInfoResponse {
    private String service;
    private String version;
    private String environment;
    private String javaVersion;
}