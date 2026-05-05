package com.subees.submanager.consumption.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAnalysisResponseDto {
    private String categoryName;
    private Integer totalAmount;
    private Integer subscriptionCount;
    private Double ratio;
}
