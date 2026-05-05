package com.subees.submanager.recommend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionItemRequest {

    @NotBlank(message = "서비스명은 필수입니다.")
    @Size(max = 100, message = "서비스명은 100자 이하여야 합니다.")
    private String serviceName;

    @Size(max = 100, message = "카테고리는 100자 이하여야 합니다.")
    private String category;

    @NotNull(message = "월 구독 금액은 필수입니다.")
    @PositiveOrZero(message = "월 구독 금액은 0 이상이어야 합니다.")
    private Integer monthlyPrice;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    private String description;
}
