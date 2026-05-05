package com.subees.submanager.subscription.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.subscription.model.dto.common.BillingCycleResponse;
import com.subees.submanager.subscription.model.dto.common.CategoryResponse;
import com.subees.submanager.subscription.model.dto.common.ItemResponse;
import com.subees.submanager.subscription.model.service.SubscriptionCommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionCommonController {

    private final SubscriptionCommonService subscriptionCommonService;

    @GetMapping("/categories")
    public ResponseEntity<BaseResponseDto<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = subscriptionCommonService.getCategories();

        return ResponseEntity.ok(
                new BaseResponseDto<>(
                        HttpStatus.OK,
                        "카테고리 목록 조회 성공",
                        categories
                )
        );
    }

    @GetMapping("/category-items/{categoryId}")
    public ResponseEntity<BaseResponseDto<List<ItemResponse>>> getItemsByCategoryId(@PathVariable Long categoryId) {
        List<ItemResponse> items = subscriptionCommonService.getItemsByCategoryId(categoryId);

        return ResponseEntity.ok(
                new BaseResponseDto<>(
                        HttpStatus.OK,
                        "카테고리별 구독항목 조회 성공",
                        items
                )
        );
    }

    @GetMapping("/billing-cycles")
    public ResponseEntity<BaseResponseDto<List<BillingCycleResponse>>> getBillingCycles() {
        List<BillingCycleResponse> billingCycles = subscriptionCommonService.getBillingCycles();

        return ResponseEntity.ok(
                new BaseResponseDto<>(
                        HttpStatus.OK,
                        "결제 주기 목록 조회 성공",
                        billingCycles
                )
        );
    }
}