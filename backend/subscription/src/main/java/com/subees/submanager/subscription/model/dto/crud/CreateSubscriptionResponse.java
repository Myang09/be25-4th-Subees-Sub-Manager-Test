package com.subees.submanager.subscription.model.dto.crud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class CreateSubscriptionResponse {
    private Long subscriptionId;
    private LocalDate createdAt;
}