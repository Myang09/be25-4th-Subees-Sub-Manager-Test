package com.subees.submanager.recommend.model.dto;

import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendDetailResponse {

    private Long reportId;
    private String reportTitle;
    private String requestNote;
    private String generatedContent;
    private Integer totalMonthlyPrice;
    private Integer maxMonthlyBudget;
    private String mandatoryItemsJson;
    private String optionalItemsJson;
    private String reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecommendSubscriptionItemResponse> subscriptionItems;

    public static RecommendDetailResponse from(RecommendReport report,
                                               List<RecommendSubscriptionItemResponse> subscriptionItems) {
        return new RecommendDetailResponse(
                report.getReportId(),
                report.getReportTitle(),
                report.getRequestNote(),
                report.getGeneratedContent(),
                report.getTotalMonthlyPrice(),
                report.getMaxMonthlyBudget(),
                report.getMandatoryItemsJson(),
                report.getOptionalItemsJson(),
                report.getReportStatus().name(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                subscriptionItems
        );
    }
}
