package com.subees.submanager.recommend.model.vo.ai;

import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiRecommendationAiClient implements RecommendationAiClient {

    @Override
    public GeneratedRecommendation generate(RecommendReport report,
                                            List<RecommendSubscriptionItem> subscriptionItems,
                                            String statsSummary) {
        throw new UnsupportedOperationException("OpenAI provider는 아직 구현되지 않았습니다.");
    }
}
