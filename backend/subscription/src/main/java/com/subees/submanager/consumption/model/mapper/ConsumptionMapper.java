package com.subees.submanager.consumption.model.mapper;

import com.subees.submanager.consumption.model.dto.CalendarResponseDto;
import com.subees.submanager.consumption.model.dto.CategoryAnalysisResponseDto;
import com.subees.submanager.consumption.model.dto.DateDetailResponseDto;
import com.subees.submanager.consumption.model.dto.MonthlyPaymentResponseDto;
import com.subees.submanager.consumption.model.vo.CategorySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsumptionMapper {

    Integer selectMonthlyTotalAmount(
            @Param("userId") Long userId,
            @Param("monthStart") String monthStart,
            @Param("nextMonthStart") String nextMonthStart
    );

    List<CalendarResponseDto> selectCalendar(
            @Param("userId") Long userId,
            @Param("monthStart") String monthStart,
            @Param("nextMonthStart") String nextMonthStart,
            @Param("month") int month
    );

    List<DateDetailResponseDto> getDetail(
            @Param("userId") Long userId,
            @Param("monthStart") String monthStart,
            @Param("nextMonthStart") String nextMonthStart,
            @Param("month") int month,
            @Param("date") Integer date
    );

    List<CategorySummary> selectCategory(
            @Param("userId") Long userId,
            @Param("monthStart") String monthStart,
            @Param("nextMonthStart") String nextMonthStart
    );

    List<CategoryAnalysisResponseDto> selectMonthlyCategoryAnalysis(
            @Param("userId") Long userId,
            @Param("monthStart") String monthStart,
            @Param("nextMonthStart") String nextMonthStart
    );

    List<CategoryAnalysisResponseDto> selectYearlyCategoryAnalysis(
            @Param("userId") Long userId,
            @Param("yearStart") String yearStart,
            @Param("nextYearStart") String nextYearStart
    );

        List<MonthlyPaymentResponseDto> selectMonthlyPaymentList(
                @Param("userId") Long userId,
                @Param("monthStart") String monthStart,
                @Param("nextMonthStart") String nextMonthStart,
                @Param("month") int month
        );

}
