package com.subees.submanager.recommend.model.vo.ai;

import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "dummy", matchIfMissing = true)
public class DummyRecommendationAiClient implements RecommendationAiClient {

    @Override
    public GeneratedRecommendation generate(RecommendReport report,
                                            List<RecommendSubscriptionItem> subscriptionItems,
                                            String statsSummary) {
        int totalMonthlyPrice = subscriptionItems.stream()
                .map(RecommendSubscriptionItem::getMonthlyPrice)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        List<RecommendSubscriptionItem> expensiveItems = subscriptionItems.stream()
                .filter(item -> item.getMonthlyPrice() != null)
                .sorted(Comparator.comparing(RecommendSubscriptionItem::getMonthlyPrice).reversed())
                .limit(3)
                .toList();

        String itemSummary = subscriptionItems.stream()
                .map(item -> String.format("- %s (%s, %,d원)",
                        item.getServiceName(),
                        safeValue(item.getCategory(), "기타"),
                        item.getMonthlyPrice() == null ? 0 : item.getMonthlyPrice()))
                .collect(Collectors.joining("\n"));

        String prioritySummary = expensiveItems.stream()
                .map(item -> String.format("- %s: 비용 비중이 높아 우선 검토 대상입니다.", item.getServiceName()))
                .collect(Collectors.joining("\n"));

        String noteSection = report.getRequestNote() == null || report.getRequestNote().isBlank()
                ? "- 별도 요청사항 없음"
                : "- 사용자 요청사항: " + report.getRequestNote();

        String generatedTitle = report.getReportTitle();
        if (generatedTitle == null || generatedTitle.isBlank()) {
            generatedTitle = "AI 구독 추천 결과";
        }

        String generatedContent = String.format(
                """
                서비스 추천 요약

                1. 현재 구독 현황
                %s

                2. 총 월 구독 비용
                - 총합: %,d원

                3. 우선 점검 포인트
                %s

                4. 추천 방향
                - 사용 빈도가 낮은 항목은 해지 또는 하위 요금제로 조정하는 것이 좋습니다.
                - 동일 카테고리 서비스가 2개 이상이면 기능 중복 여부를 먼저 확인하는 것이 좋습니다.
                - 업무용/학습용 서비스는 유지 우선순위를 높게 두고, 취미/엔터테인먼트 서비스는 사용량 기준으로 조정합니다.

                5. 요청사항 반영
                %s

                6. 설문 통계 참고
                %s

                ※ 현재 응답은 더미 AI 결과입니다. 추후 외부 AI 연동 시 동일 인터페이스를 유지한 채 실제 생성 결과로 교체할 수 있습니다.
                """,
                itemSummary,
                totalMonthlyPrice,
                prioritySummary.isBlank() ? "- 점검 대상이 없습니다." : prioritySummary,
                noteSection,
                safeValue(statsSummary, "설문 통계 없음")
        );

        return new GeneratedRecommendation(generatedTitle, generatedContent);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
