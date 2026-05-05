package com.subees.submanager.consumption.model.vo;


import lombok.Getter;

@Getter
public enum BillingCycle {
    ONE_MONTH("1M"),
    ONE_YEAR("1Y");

    private final String code;

    BillingCycle(String code) {
        this.code = code;
    }

}
