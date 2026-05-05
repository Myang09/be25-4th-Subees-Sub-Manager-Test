package com.subees.submanager.consumption.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Consumption {
    private long userId;

    private Long subscriptionId;

    private Long itemId;

    private Long paymentId;

    private BillingCycle billingCycle;

    private LocalDate startDate;

    private String createdAt;

    private String updatedAt;

    private char useYn;

    private String endDate;

    private int price;


}
