package com.subees.submanager.recommend.model.vo.ai;

import com.subees.submanager.recommend.model.vo.recommend.RecommendReport;
import com.subees.submanager.recommend.model.vo.recommend.RecommendSubscriptionItem;

import java.util.List;

public interface RecommendationAiClient {

    GeneratedRecommendation generate(RecommendReport report,
                                     List<RecommendSubscriptionItem> subscriptionItems,
                                     String statsSummary);
}
