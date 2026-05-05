package com.subees.submanager.recommend.model.vo.recommend;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendReport {

    private Long reportId;
    private Long userId;
    private String reportTitle;
    private String requestNote;
    private String generatedContent;
    private Integer totalMonthlyPrice;
    private Integer maxMonthlyBudget;
    private String mandatoryItemsJson;
    private String optionalItemsJson;
    private RecommendReportStatus reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public RecommendReport(Long userId,
                           String reportTitle,
                           String requestNote,
                           String generatedContent,
                           Integer totalMonthlyPrice,
                           Integer maxMonthlyBudget,
                           String mandatoryItemsJson,
                           String optionalItemsJson,
                           RecommendReportStatus reportStatus,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.userId = userId;
        this.reportTitle = reportTitle;
        this.requestNote = requestNote;
        this.generatedContent = generatedContent;
        this.totalMonthlyPrice = totalMonthlyPrice;
        this.maxMonthlyBudget = maxMonthlyBudget;
        this.mandatoryItemsJson = mandatoryItemsJson;
        this.optionalItemsJson = optionalItemsJson;
        this.reportStatus = reportStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
