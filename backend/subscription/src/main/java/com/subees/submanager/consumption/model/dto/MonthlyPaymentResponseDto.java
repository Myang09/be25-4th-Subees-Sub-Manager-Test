package com.subees.submanager.consumption.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPaymentResponseDto {

    private Long paymentId;
    private String subscriptionName;
    private String categoryName;
    private String paymentCardName;
    private int paymentAmount;
    private String displayDateLabel;
    private boolean isYearly;
}