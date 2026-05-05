package com.subees.submanager.consumption.model.service;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.consumption.model.dto.CalendarResponseDto;
import com.subees.submanager.consumption.model.dto.CalendarResultDto;
import com.subees.submanager.consumption.model.dto.CategoryAnalysisResponseDto;
import com.subees.submanager.consumption.model.dto.CategoryAnalysisResultDto;
import com.subees.submanager.consumption.model.dto.CategoryResponseDto;
import com.subees.submanager.consumption.model.dto.DateDetailResponseDto;
import com.subees.submanager.consumption.model.dto.MonthlyPaymentResponseDto;
import com.subees.submanager.consumption.model.mapper.ConsumptionMapper;
import com.subees.submanager.consumption.model.vo.CategorySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumptionServiceImpl implements ConsumptionService {

    private final ConsumptionMapper consumptionMapper;

    /*
    * 캘린더 전체 조회
    * - 연도, 월 조건 외 예외처리
    */
    @Override
    public CalendarResultDto getCalendarSummary(Long userId, int year, int month) {

        if (year <= 0) {
            throw new UniversityException(ExceptionMessage.INVALID_YEAR);
        }

        if (month < 1 || month > 12) {
            throw new UniversityException(ExceptionMessage.INVALID_MONTH);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        Integer monthTotalAmount = consumptionMapper.selectMonthlyTotalAmount(
                userId,
                monthStart.toString(),
                nextMonthStart.toString()
        );

        List<CalendarResponseDto> items = consumptionMapper.selectCalendar(
                userId,
                monthStart.toString(),
                nextMonthStart.toString(),
                month
        );

        return new CalendarResultDto(year, month, monthTotalAmount, items);
    }

    // 해달 날짜 상세 조회
    @Override
    public List<DateDetailResponseDto> getDetail(Long userId, int year, int month, Integer date) {

        if (year <= 0) {
            throw new UniversityException(ExceptionMessage.INVALID_YEAR);
        }

        if (month < 1 || month > 12) {
            throw new UniversityException(ExceptionMessage.INVALID_MONTH);
        }

        if (date == null) {
            throw new  UniversityException(ExceptionMessage.INVALID_DATE);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        int lastDay = yearMonth.lengthOfMonth(); // 그 달의 마지막 수 반환

        if (date < 1 || date > lastDay) {
            throw new UniversityException(ExceptionMessage.INVALID_DATE);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        return consumptionMapper.getDetail(
                userId,
                monthStart.toString(),
                nextMonthStart.toString(),
                month,
                date
        );
    }

    // 카테고리 조회
    @Override
    public List<CategoryResponseDto> getCategory(Long userId, int year, int month) {

        if (year <= 0) {
            throw new UniversityException(ExceptionMessage.INVALID_YEAR);
        }

        if (month < 1 || month > 12) {
            throw new UniversityException(ExceptionMessage.INVALID_MONTH);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        List<CategorySummary> category = consumptionMapper.selectCategory(
                userId,
                monthStart.toString(),
                nextMonthStart.toString()
        );

        return category.stream()
                .map(vo -> new CategoryResponseDto(
                        year,
                        month,
                        vo.getCategoryName(),
                        vo.getTotalAmount(),
                        vo.getItemNames()
                ))
                .toList();
    }

    // 카테고리 분석
    @Override
    public CategoryAnalysisResultDto getCategoryAnalysis(Long userId, int year, Integer month, String rangeType) {

        if (year <= 0) {
            throw new UniversityException(ExceptionMessage.INVALID_YEAR);
        }

        if (rangeType == null || rangeType.isBlank()) {
            throw new UniversityException(ExceptionMessage.RANGE_REQUIRED);
        }

        if (!"MONTH".equals(rangeType) && !"YEAR".equals(rangeType)) {
            throw new UniversityException(ExceptionMessage.INVALID_RANGE_TYPE);
        }

        if ("MONTH".equals(rangeType)) {
            if (month == null) {
                throw new UniversityException(ExceptionMessage. MONTH_REQUIRED);
            }

            if (month < 1 || month > 12) {
                throw new UniversityException(ExceptionMessage.INVALID_MONTH);
            }
        }

        List<CategoryAnalysisResponseDto> categories;

        if ("MONTH".equals(rangeType)) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate nextMonthStart = monthStart.plusMonths(1);

            categories = consumptionMapper.selectMonthlyCategoryAnalysis(
                    userId,
                    monthStart.toString(),
                    nextMonthStart.toString()
            );
        } else {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate nextYearStart = yearStart.plusYears(1);

            categories = consumptionMapper.selectYearlyCategoryAnalysis(
                    userId,
                    yearStart.toString(),
                    nextYearStart.toString()
            );
        }

        int totalAmount = categories.stream()
                .mapToInt(category -> category.getTotalAmount() == null ? 0 : category.getTotalAmount())
                .sum();

        for (CategoryAnalysisResponseDto category : categories) {
            double ratio = totalAmount == 0
                    ? 0.0
                    : (category.getTotalAmount() * 100.0 / totalAmount);

            category.setRatio(Math.round(ratio * 10) / 10.0);
        }

        return new CategoryAnalysisResultDto(year, month, rangeType, totalAmount, categories);
    }

    /*
    월별 분석
     */
    @Override
    public List<MonthlyPaymentResponseDto> getMonthlyPaymentList(Long userId, int year, int month) {
        if (userId == null) {
            throw new UniversityException(ExceptionMessage.USER_NOT_FOUND);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        return consumptionMapper.selectMonthlyPaymentList(
                userId,
                monthStart.toString(),
                nextMonthStart.toString(),
                month
        );
    }

}