package com.subees.submanager.subscription.model.dto.crud;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubscriptionRequest {

    private Long categoryId;
    private Long itemId;       // 기존 항목 선택 시 사용
    private String itemName;   // 직접 입력 시 사용

    private Long paymentId;
    private Integer price;
    private String billingCycle;
    private LocalDate startDate;
}