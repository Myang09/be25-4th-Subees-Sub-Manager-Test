package com.subees.submanager.subscription.model.dto.crud;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
public class UpdateSubscriptionRequest {

    private Long categoryId;

    private String categoryName;   // 직접 입력용이면 나중에 사용

    private Long itemId;

    private String itemName;        // 직접 입력용이면 나중에 사용

    private Integer price;

    private String billingCycle;

    private LocalDate startDate;

    private Long paymentId;
}