package com.subees.submanager.recommend.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.recommend.model.vo.ai.GeneratedRecommendation;
import com.subees.submanager.recommend.model.vo.ai.RecommendationAiClient;
import com.subees.submanager.recommend.model.dto.PopularSubscriptionStat;
import com.subees.submanager.recommend.model.dto.RecommendDeleteResponse;
import com.subees.submanager.recommend.model.dto.RecommendDetailResponse;
import com.subees.submanager.recommend.model.dto.RecommendGenerateRequest;
import com.subees.submanager.recommend.model.dto.RecommendGenerateResponse;
import com.subees.submanager.recommend.model.dto.RecommendListItemResponse;
import com.subees.submanager.recommend.model.dto.RecommendListResponse;
import com.subees.submanager.recommend.model.dto.RecommendSaveRequest;
import com.subees.submanager.recommend.model.dto.RecommendSaveResponse;
import com.subees.submanager.recommend.model.dto.RecommendSubmitRequest;
import com.subees.submanager.recommend.model.dto.RecommendSubmitResponse;
import com.subees.submanager.recommend.model.dto.RecommendSubscriptionItemResponse;
import com.subees.submanager.recommend.model.dto.SubscriptionItemRequest;
import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import com.subees.submanager.recommend.model.vo.recommend.RecommendReportStatus;
import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;
import com.subees.submanager.recommend.model.mapper.RecommendMapper;
import com.subees.submanager.recommend.model.mapper.RecommendStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

    private final RecommendMapper recommendMapper;
    private final RecommendationAiClient recommendationAiClient;
    private final RecommendStatusMapper recommendStatusMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public RecommendSubmitResponse submit(Long userId, RecommendSubmitRequest request) {
        LocalDateTime now = LocalDateTime.now();
        int totalMonthlyPrice = calculateTotalMonthlyPrice(request.getSubscriptionItems());

        RecommendReport report = RecommendReport.builder()
                .userId(userId)
                .reportTitle(resolveInitialTitle(request.getReportTitle()))
                .requestNote(request.getRequestNote())
                .generatedContent(null)
                .totalMonthlyPrice(totalMonthlyPrice)
                .maxMonthlyBudget(request.getMaxMonthlyBudget())
                .mandatoryItemsJson(toJson(request.getMandatoryItems()))
                .optionalItemsJson(toJson(request.getOptionalItems()))
                .reportStatus(RecommendReportStatus.SUBMITTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        recommendMapper.insertReport(report);

        for (int i = 0; i < request.getSubscriptionItems().size(); i++) {
            SubscriptionItemRequest item = request.getSubscriptionItems().get(i);
            recommendMapper.insertSubscriptionItem(
                    RecommendSubscriptionItem.builder()
                            .reportId(report.getReportId())
                            .serviceName(item.getServiceName())
                            .category(item.getCategory())
                            .monthlyPrice(item.getMonthlyPrice())
                            .description(item.getDescription())
                            .sortOrder(i + 1)
                            .createdAt(now)
                            .build()
            );
        }

        return new RecommendSubmitResponse(
                report.getReportId(),
                report.getReportTitle(),
                report.getReportStatus().name(),
                request.getSubscriptionItems().size(),
                totalMonthlyPrice
        );
    }

    @Transactional
    public RecommendGenerateResponse generate(Long userId, RecommendGenerateRequest request) {
        RecommendReport report = findReportOrThrow(request.getReportId(), userId);
        List<RecommendSubscriptionItem> items = recommendMapper.findItemsByReportId(report.getReportId());

        if (items.isEmpty()) {
            throw new IllegalArgumentException("추천 생성을 위한 구독 항목이 없습니다.");
        }

        List<PopularSubscriptionStat> topStats = recommendStatusMapper.findTopPopularSubscriptions(5);
        String statsSummary = topStats.stream()
                .map(stat -> String.format("- %s (%s), 구독 수: %d, 평균 가격: %d원",
                        stat.getItemName(),
                        stat.getCategoryName(),
                        stat.getSubscribeCount(),
                        stat.getAvgPrice()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("설문 통계 없음");

        GeneratedRecommendation generatedRecommendation = recommendationAiClient.generate(report, items, statsSummary);
        int totalMonthlyPrice = items.stream()
                .map(RecommendSubscriptionItem::getMonthlyPrice)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        recommendMapper.updateGeneratedResult(
                report.getReportId(),
                generatedRecommendation.getReportTitle(),
                generatedRecommendation.getGeneratedContent(),
                totalMonthlyPrice,
                RecommendReportStatus.GENERATED.name()
        );

        return new RecommendGenerateResponse(
                report.getReportId(),
                generatedRecommendation.getReportTitle(),
                generatedRecommendation.getGeneratedContent(),
                totalMonthlyPrice,
                RecommendReportStatus.GENERATED.name()
        );
    }

    @Transactional
    public RecommendSaveResponse save(Long userId, RecommendSaveRequest request) {
        RecommendReport report = findReportOrThrow(request.getReportId(), userId);

        String finalTitle = hasText(request.getReportTitle())
                ? request.getReportTitle()
                : report.getReportTitle();

        String finalContent = hasText(request.getGeneratedContent())
                ? request.getGeneratedContent()
                : report.getGeneratedContent();

        if (!hasText(finalContent)) {
            throw new IllegalArgumentException("저장할 추천 내용이 없습니다. 먼저 추천을 생성해주세요.");
        }

        recommendMapper.saveReport(
                report.getReportId(),
                finalTitle,
                finalContent,
                RecommendReportStatus.SAVED.name()
        );

        return new RecommendSaveResponse(
                report.getReportId(),
                finalTitle,
                RecommendReportStatus.SAVED.name(),
                LocalDateTime.now().toString()
        );
    }

    public RecommendListResponse getSavedReports(Long userId) {
        List<RecommendListItemResponse> reports = recommendMapper.findSavedReportsByUserId(userId).stream()
                .map(RecommendListItemResponse::from)
                .toList();

        return new RecommendListResponse(reports.size(), reports);
    }

    public RecommendDetailResponse getReportDetail(Long userId, Long reportId) {
        RecommendReport report = findReportOrThrow(reportId, userId);
        List<RecommendSubscriptionItemResponse> subscriptionItems = recommendMapper.findItemsByReportId(reportId).stream()
                .map(RecommendSubscriptionItemResponse::from)
                .toList();

        return RecommendDetailResponse.from(report, subscriptionItems);
    }

    @Transactional
    public RecommendDeleteResponse delete(Long userId, Long reportId) {
        findReportOrThrow(reportId, userId);
        recommendMapper.deleteItemsByReportId(reportId);
        int deletedCount = recommendMapper.deleteReportByIdAndUserId(reportId, userId);
        return new RecommendDeleteResponse(reportId, deletedCount > 0);
    }

    private RecommendReport findReportOrThrow(Long reportId, Long userId) {
        RecommendReport report = recommendMapper.findReportById(reportId);

        if (report == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 리포트를 찾을 수 없습니다.");
        }

        if (!report.getUserId().equals(userId)) {
            throw new UniversityException(ExceptionMessage.RECOMMEND_ACCESS_DENIED);
        }

        return report;
    }

    private int calculateTotalMonthlyPrice(List<SubscriptionItemRequest> items) {
        return items.stream()
                .map(SubscriptionItemRequest::getMonthlyPrice)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String resolveInitialTitle(String reportTitle) {
        if (hasText(reportTitle)) {
            return reportTitle;
        }
        return "AI 구독 추천 결과 " + LocalDate.now();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("구독 조건 목록 JSON 변환에 실패했습니다.", e);
        }
    }
}
