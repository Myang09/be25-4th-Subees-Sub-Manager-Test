package com.subees.submanager.recommend.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PopularSubscriptionStat {

    private String itemName;
    private String categoryName;
    private Integer subscribeCount;
    private Integer avgPrice;
}
