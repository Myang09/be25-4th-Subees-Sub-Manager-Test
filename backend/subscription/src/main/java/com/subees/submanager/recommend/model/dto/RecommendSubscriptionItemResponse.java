package com.subees.submanager.recommend.model.dto;

import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendSubscriptionItemResponse {

    private Long itemId;
    private String serviceName;
    private String category;
    private Integer monthlyPrice;
    private String description;
    private Integer sortOrder;

    public static RecommendSubscriptionItemResponse from(RecommendSubscriptionItem item) {
        return new RecommendSubscriptionItemResponse(
                item.getRecommendItemId(),
                item.getServiceName(),
                item.getCategory(),
                item.getMonthlyPrice(),
                item.getDescription(),
                item.getSortOrder()
        );
    }
}
