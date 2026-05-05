package com.subees.submanager.system.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.system.dto.HealthCheckResponse;
import com.subees.submanager.system.dto.SystemInfoResponse;
import com.subees.submanager.system.dto.SystemTimeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/health")
    public ResponseEntity<BaseResponseDto<HealthCheckResponse>> health() {
        HealthCheckResponse response = new HealthCheckResponse(
                "UP",
                "SUBEES_BACK",
                ZonedDateTime.now().toString()
        );
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, response));
    }

    @GetMapping("/info")
    public ResponseEntity<BaseResponseDto<SystemInfoResponse>> info() {
        SystemInfoResponse response = new SystemInfoResponse(
                "SUBEES_BACK",
                "v1",
                "local",
                System.getProperty("java.version")
        );
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, response));
    }

    @GetMapping("/time")
    public ResponseEntity<BaseResponseDto<SystemTimeResponse>> time() {
        ZonedDateTime now = ZonedDateTime.now();
        SystemTimeResponse response = new SystemTimeResponse(
                now.toString(),
                now.getZone().getId(),
                now.toInstant().toEpochMilli()
        );
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, response));
    }
}
