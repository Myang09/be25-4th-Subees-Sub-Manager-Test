package com.subees.submanager.recommend.model.dto;

import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecommendListItemResponse {

    private Long reportId;
    private String reportTitle;
    private Integer totalMonthlyPrice;
    private String reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecommendListItemResponse from(RecommendReport report) {
        return new RecommendListItemResponse(
                report.getReportId(),
                report.getReportTitle(),
                report.getTotalMonthlyPrice(),
                report.getReportStatus().name(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
