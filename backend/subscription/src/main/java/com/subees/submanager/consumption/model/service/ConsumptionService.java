package com.subees.submanager.consumption.model.service;

import com.subees.submanager.consumption.model.dto.CalendarResultDto;
import com.subees.submanager.consumption.model.dto.CategoryAnalysisResultDto;
import com.subees.submanager.consumption.model.dto.CategoryResponseDto;
import com.subees.submanager.consumption.model.dto.DateDetailResponseDto;
import com.subees.submanager.consumption.model.dto.MonthlyPaymentResponseDto;

import java.util.List;

public interface ConsumptionService {
    CalendarResultDto getCalendarSummary(Long userId, int year, int month);

    List<DateDetailResponseDto> getDetail(Long userId, int year, int month, Integer date);

    List<CategoryResponseDto> getCategory(Long userId, int year, int month);

    CategoryAnalysisResultDto getCategoryAnalysis(Long userId, int year, Integer month, String rangeType);

    List<MonthlyPaymentResponseDto> getMonthlyPaymentList(Long userId, int year, int month);
}
