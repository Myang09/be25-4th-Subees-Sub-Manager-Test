package com.subees.submanager.subscription.model.service;

import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionListResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    List<SubscriptionListResponse> getSubscriptions(Long userId);

    CreateSubscriptionResponse createSubscription(Long userId, CreateSubscriptionRequest request);

    SubscriptionResponse getSubscriptionById(Long userId, Long subscriptionId);

    UpdateSubscriptionResponse updateSubscription(Long userId, Long subscriptionId, UpdateSubscriptionRequest request);

    void deleteSubscription(Long userId, Long subscriptionId);

}