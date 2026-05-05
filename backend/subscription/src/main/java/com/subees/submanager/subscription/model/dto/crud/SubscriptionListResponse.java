package com.subees.submanager.subscription.model.dto.crud;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionListResponse {
    private Long subscriptionId;
    private String categoryName;
    private String itemName;
    private Integer price;
    private String billingCycle;
    private String paymentMethodName;
    private LocalDate startDate;
    private LocalDate createdAt;
    private LocalDate nextPaymentDate;
    private String useYn;
}