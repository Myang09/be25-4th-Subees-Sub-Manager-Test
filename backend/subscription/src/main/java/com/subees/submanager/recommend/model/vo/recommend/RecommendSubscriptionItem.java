package com.subees.submanager.recommend.model.vo.recommend;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendSubscriptionItem {

    private Long recommendItemId;
    private Long reportId;
    private String serviceName;
    private String category;
    private Integer monthlyPrice;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    @Builder
    public RecommendSubscriptionItem(Long reportId,
                                     String serviceName,
                                     String category,
                                     Integer monthlyPrice,
                                     String description,
                                     Integer sortOrder,
                                     LocalDateTime createdAt) {
        this.reportId = reportId;
        this.serviceName = serviceName;
        this.category = category;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }
}
