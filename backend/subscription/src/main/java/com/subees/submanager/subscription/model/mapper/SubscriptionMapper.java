package com.subees.submanager.subscription.model.mapper;

import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionListResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionRequest;
import com.subees.submanager.subscription.model.vo.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubscriptionMapper {

    List<SubscriptionListResponse> selectAll(@Param("userId") Long userId);

    int countDuplicateSubscription(@Param("userId") Long userId,
                                   @Param("request") CreateSubscriptionRequest request);

    void insertSubscription(@Param("userId") Long userId,
                            @Param("request") CreateSubscriptionRequest request);

    Long selectLastInsertedId();

    SubscriptionResponse selectById(@Param("userId") Long userId,
                                    @Param("subscriptionId") Long subscriptionId);

    Subscription selectSubscriptionById(@Param("userId") Long userId,
                                        @Param("subscriptionId") Long subscriptionId);

    int updateSubscription(@Param("userId") Long userId,
                           @Param("subscriptionId") Long subscriptionId,
                           @Param("request") UpdateSubscriptionRequest request);

    int existsPaymentMethod(@Param("paymentId") Long paymentId);

    int softDeleteSubscription(@Param("userId") Long userId,
                               @Param("subscriptionId") Long subscriptionId);

    int existsCategory(Long categoryId);

    int existsItem(Long itemId);

    int existsItemInCategory(Long categoryId, Long itemId);

    Long findItemIdByCategoryAndName(@Param("categoryId") Long categoryId,
                                     @Param("itemName") String itemName);

    int insertSubscriptionItem(@Param("categoryId") Long categoryId,
                               @Param("itemName") String itemName);

}