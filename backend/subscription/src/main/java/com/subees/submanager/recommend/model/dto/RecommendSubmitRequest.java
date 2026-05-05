package com.subees.submanager.recommend.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendSubmitRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String reportTitle;

    @Size(max = 2000, message = "요청 메모는 2000자 이하여야 합니다.")
    private String requestNote;

    @PositiveOrZero(message = "최대 월 예산은 0 이상이어야 합니다.")
    private Integer maxMonthlyBudget;

    @Size(max = 30, message = "필수 구독 항목은 최대 30개까지 가능합니다.")
    private List<@Size(max = 100, message = "필수 구독 항목명은 100자 이하여야 합니다.") String> mandatoryItems;

    @Size(max = 30, message = "추가 희망 구독 항목은 최대 30개까지 가능합니다.")
    private List<@Size(max = 100, message = "추가 희망 구독 항목명은 100자 이하여야 합니다.") String> optionalItems;

    @Valid
    @NotEmpty(message = "구독 항목은 최소 1개 이상이어야 합니다.")
    private List<SubscriptionItemRequest> subscriptionItems;
}
