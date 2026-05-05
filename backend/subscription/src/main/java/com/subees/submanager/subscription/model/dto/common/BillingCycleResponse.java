package com.subees.submanager.subscription.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BillingCycleResponse {
    private String code;
    private String label;
}