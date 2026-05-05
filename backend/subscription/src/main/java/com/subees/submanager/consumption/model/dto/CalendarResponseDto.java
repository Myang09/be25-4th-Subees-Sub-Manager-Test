package com.subees.submanager.consumption.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    private Integer payDay;
    private Integer totalCount;
    private Integer totalAmount;
    private String itemNames;
}
