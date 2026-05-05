package com.subees.submanager.consumption.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.common.model.dto.ItemsResponseDto;
import com.subees.submanager.consumption.model.dto.CalendarResultDto;
import com.subees.submanager.consumption.model.dto.CategoryAnalysisResultDto;
import com.subees.submanager.consumption.model.dto.CategoryResponseDto;
import com.subees.submanager.consumption.model.dto.DateDetailResponseDto;
import com.subees.submanager.consumption.model.dto.MonthlyPaymentResponseDto;
import com.subees.submanager.consumption.model.service.ConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumption")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    // 캘린더 전체 조회
    @GetMapping("/calendar-summary")
    public ResponseEntity<BaseResponseDto<CalendarResultDto>> getCalendarSummary(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {

        Long userId = (Long) authentication.getPrincipal();
        CalendarResultDto result = consumptionService.getCalendarSummary(userId, year, month);
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, result));
    }

    // 해달 일 상세 조회
    @GetMapping("/date-details")
    public ResponseEntity<ItemsResponseDto<DateDetailResponseDto>> getDateDetails(Authentication authentication,
                                                                                  @RequestParam int year,
                                                                                  @RequestParam int month,
                                                                                  @RequestParam Integer date) {

        Long userId = (Long) authentication.getPrincipal();
        List<DateDetailResponseDto> items = consumptionService.getDetail(userId, year, month, date);

        return ResponseEntity.ok(new ItemsResponseDto<>(HttpStatus.OK, items, 1, items.size()));
    }

    // 카테코리 전체 조회
    @GetMapping("/category-summary")
    public ResponseEntity<ItemsResponseDto<CategoryResponseDto>> getCategory(Authentication authentication, @RequestParam int year, @RequestParam int month) {

        Long userId = (Long) authentication.getPrincipal();
        List<CategoryResponseDto> items = consumptionService.getCategory(userId, year, month);
        return ResponseEntity.ok(new ItemsResponseDto<>(HttpStatus.OK, items, 1, items.size()));
    }

    // 카테고리 분석
    @GetMapping("/analysis/categories")
    public ResponseEntity<BaseResponseDto<CategoryAnalysisResultDto>> getCategoryAnalysis(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam String rangeType
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CategoryAnalysisResultDto result = consumptionService.getCategoryAnalysis(userId, year, month, rangeType);
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, result));
    }

    @GetMapping("/analysis/month")
    public ResponseEntity<BaseResponseDto<List<MonthlyPaymentResponseDto>>> getMonthlyPaymentList(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long userId = (Long) authentication.getPrincipal();

        List<MonthlyPaymentResponseDto> result =
                consumptionService.getMonthlyPaymentList(userId, year, month);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, result)
        );
    }


}
