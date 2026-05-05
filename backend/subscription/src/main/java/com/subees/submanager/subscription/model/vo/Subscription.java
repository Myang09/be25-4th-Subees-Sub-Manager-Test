package com.subees.submanager.subscription.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    private Long subscriptionId;
    private Long itemId;
    private Long userId;
    private Long paymentId;
    private Integer price;
    private String billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}