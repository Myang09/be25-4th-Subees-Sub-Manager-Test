package com.subees.submanager.recommend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecommendGenerateRequest {

    @NotNull(message = "reportId는 필수입니다.")
    private Long reportId;
}
