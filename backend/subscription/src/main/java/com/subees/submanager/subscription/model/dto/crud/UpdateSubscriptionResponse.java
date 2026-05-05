package com.subees.submanager.subscription.model.dto.crud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class UpdateSubscriptionResponse {
    private Long subscriptionId;
    private LocalDate updatedAt;
}

