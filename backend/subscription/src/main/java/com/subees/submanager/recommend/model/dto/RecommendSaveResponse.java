package com.subees.submanager.recommend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendSaveResponse {

    private Long reportId;
    private String reportTitle;
    private String reportStatus;
    private String savedAt;
}
