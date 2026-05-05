package com.subees.submanager.recommend.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.recommend.model.dto.RecommendDeleteResponse;
import com.subees.submanager.recommend.model.dto.RecommendDetailResponse;
import com.subees.submanager.recommend.model.dto.RecommendGenerateRequest;
import com.subees.submanager.recommend.model.dto.RecommendGenerateResponse;
import com.subees.submanager.recommend.model.dto.RecommendListResponse;
import com.subees.submanager.recommend.model.dto.RecommendSaveRequest;
import com.subees.submanager.recommend.model.dto.RecommendSaveResponse;
import com.subees.submanager.recommend.model.dto.RecommendSubmitRequest;
import com.subees.submanager.recommend.model.dto.RecommendSubmitResponse;
import com.subees.submanager.recommend.model.service.RecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/submit")
    public ResponseEntity<BaseResponseDto<RecommendSubmitResponse>> submit(@RequestBody @Valid RecommendSubmitRequest request,
                                                                           Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, recommendService.submit(tokenUserId, request)));
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponseDto<RecommendGenerateResponse>> generate(@RequestBody @Valid RecommendGenerateRequest request,
                                                                               Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, recommendService.generate(tokenUserId, request)));
    }

    @PostMapping("/save")
    public ResponseEntity<BaseResponseDto<RecommendSaveResponse>> save(@RequestBody @Valid RecommendSaveRequest request,
                                                                       Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, recommendService.save(tokenUserId, request)));
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponseDto<RecommendListResponse>> getSavedReports(Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, recommendService.getSavedReports(tokenUserId)));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<BaseResponseDto<RecommendDetailResponse>> getReportDetail(@PathVariable Long reportId,
                                                                                     Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, recommendService.getReportDetail(tokenUserId, reportId)));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<BaseResponseDto<RecommendDeleteResponse>> delete(@PathVariable Long reportId,
                                                                           Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "추천 리포트 삭제 성공", recommendService.delete(tokenUserId, reportId))
        );
    }
}
