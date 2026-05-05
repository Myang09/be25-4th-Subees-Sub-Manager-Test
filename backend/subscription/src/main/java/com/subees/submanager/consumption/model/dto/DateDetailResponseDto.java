package com.subees.submanager.consumption.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DateDetailResponseDto {

    private Long subscriptionId;

    private String itemName;

    private int billingDay;

    private int price;

    private String customCardCompany;

    private String cardCompany;




}
