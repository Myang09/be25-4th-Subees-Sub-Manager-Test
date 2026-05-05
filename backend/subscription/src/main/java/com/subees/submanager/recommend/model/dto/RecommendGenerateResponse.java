package com.subees.submanager.recommend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendGenerateResponse {

    private Long reportId;
    private String reportTitle;
    private String generatedContent;
    private Integer totalMonthlyPrice;
    private String reportStatus;
}
