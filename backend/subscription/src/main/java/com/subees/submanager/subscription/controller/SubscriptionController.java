package com.subees.submanager.subscription.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionListResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionResponse;
import com.subees.submanager.subscription.model.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<SubscriptionListResponse>>> getSubscriptions(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<SubscriptionListResponse> response = subscriptionService.getSubscriptions(userId);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "구독 목록 조회 성공", response)
        );
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<BaseResponseDto<SubscriptionResponse>> getSubscriptionById(
            @PathVariable Long subscriptionId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        SubscriptionResponse response = subscriptionService.getSubscriptionById(userId, subscriptionId);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "구독 상세 조회 성공", response)
        );
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<CreateSubscriptionResponse>> createSubscription(
            @RequestBody CreateSubscriptionRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CreateSubscriptionResponse response = subscriptionService.createSubscription(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, "구독 항목이 성공적으로 등록되었습니다.", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDto<UpdateSubscriptionResponse>> updateSubscription(
            @PathVariable("id") Long subscriptionId,
            @RequestBody UpdateSubscriptionRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        UpdateSubscriptionResponse response =
                subscriptionService.updateSubscription(userId, subscriptionId, request);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "수정이 완료되었습니다.", response)
        );
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<BaseResponseDto<Void>> deleteSubscription(
            @PathVariable Long subscriptionId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionService.deleteSubscription(userId, subscriptionId);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "구독이 삭제되었습니다.", null)
        );
    }
}