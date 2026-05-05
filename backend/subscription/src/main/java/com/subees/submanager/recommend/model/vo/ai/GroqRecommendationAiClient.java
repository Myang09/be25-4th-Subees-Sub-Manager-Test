package com.subees.submanager.recommend.model.vo.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "groq")
public class GroqRecommendationAiClient implements RecommendationAiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.groq.api-key:}")
    private String apiKey;

    @Value("${app.ai.groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${app.ai.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Override
    public GeneratedRecommendation generate(RecommendReport report,
                                            List<RecommendSubscriptionItem> subscriptionItems,
                                            String statsSummary) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API 키가 설정되지 않았습니다.");
        }

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        String itemsText = subscriptionItems.stream()
                .map(item -> String.format(
                        "- 서비스명: %s / 카테고리: %s / 월 금액: %d / 설명: %s",
                        safe(item.getServiceName()),
                        safe(item.getCategory()),
                        item.getMonthlyPrice() == null ? 0 : item.getMonthlyPrice(),
                        safe(item.getDescription())
                ))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                """
                너는 구독 서비스 정리 및 추천 도우미다.
                아래 정보를 바탕으로 한국어 추천 리포트를 작성해라.

                반드시 다음 규칙을 지켜라.
                1. 필수 구독은 유지해야 한다.
                2. 최대 월 예산을 가능하면 넘지 않도록 추천해야 한다.
                3. 있으면 좋은 구독은 예산이 허용할 때만 추천한다.
                4. 현재 구독 중 불필요하거나 중복 가능성이 있는 항목은 검토 대상으로 제시할 수 있다.
                5. 응답은 반드시 자연스러운 한국어만 사용하고, 다른 언어 또는 한자가 섞이지 않도록 작성하라.
                6. 설문 통계가 주어지면 그 내용을 추천 근거에 자연스럽게 반영하라.

                반드시 아래 내용을 포함해라.
                1. 현재 구독 요약
                2. 예산 분석
                3. 유지 추천 항목
                4. 해지 또는 검토 추천 항목
                5. 추가 추천 항목
                6. 추천 이유
                7. 예상 월 구독 비용
                8. 한 줄 요약

                [현재 제목]
                %s

                [사용자 요청사항]
                %s

                [최대 월 예산]
                %s

                [필수 구독 목록]
                %s

                [있으면 좋은 구독 목록]
                %s

                [설문 통계]
                %s

                [현재 구독 목록]
                %s
                """,
                safe(report.getReportTitle()),
                safe(report.getRequestNote()),
                report.getMaxMonthlyBudget() == null ? "미입력" : report.getMaxMonthlyBudget() + "원",
                safe(report.getMandatoryItemsJson()),
                safe(report.getOptionalItemsJson()),
                safe(statsSummary),
                itemsText
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 구독 비용 최적화 전문가입니다."),
                Map.of("role", "user", "content", prompt)
        ));
        payload.put("temperature", 0.5);

        String responseBody = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            String title = (report.getReportTitle() == null || report.getReportTitle().isBlank())
                    ? "AI 구독 추천 결과"
                    : report.getReportTitle();

            return new GeneratedRecommendation(title, content);
        } catch (Exception e) {
            throw new IllegalStateException("Groq 응답 파싱 중 오류가 발생했습니다.", e);
        }
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "없음" : value;
    }
}
