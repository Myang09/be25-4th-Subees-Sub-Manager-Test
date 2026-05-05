package com.subees.submanager.subscription.model.service;

import com.subees.submanager.subscription.model.dto.common.BillingCycleResponse;
import com.subees.submanager.subscription.model.dto.common.CategoryResponse;
import com.subees.submanager.subscription.model.dto.common.ItemResponse;

import java.util.List;

public interface SubscriptionCommonService {

    List<CategoryResponse> getCategories();

    List<ItemResponse> getItemsByCategoryId(Long categoryId);

    List<BillingCycleResponse> getBillingCycles();
}