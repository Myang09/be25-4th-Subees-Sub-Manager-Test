package com.subees.submanager.recommend.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecommendSaveRequest {

    @NotNull(message = "reportId는 필수입니다.")
    private Long reportId;

    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String reportTitle;

    @Size(max = 10000, message = "추천 내용은 10000자 이하여야 합니다.")
    private String generatedContent;
}
