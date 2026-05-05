package com.subees.submanager.subscription.model.service;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.CreateSubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionListResponse;
import com.subees.submanager.subscription.model.dto.crud.SubscriptionResponse;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionRequest;
import com.subees.submanager.subscription.model.dto.crud.UpdateSubscriptionResponse;
import com.subees.submanager.subscription.model.mapper.SubscriptionMapper;
import com.subees.submanager.subscription.model.vo.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;

    @Override
    public List<SubscriptionListResponse> getSubscriptions(Long userId) {
        return subscriptionMapper.selectAll(userId);
    }

//    @Override
//    public CreateSubscriptionResponse createSubscription(Long userId, CreateSubscriptionRequest request) {
//        if (request.getPrice() == null || request.getPrice() < 0) {
//            throw new IllegalArgumentException("결제금액이 0원 이상이어야 합니다.");
//        }
//
//        if (request.getItemId() == null) {
//            throw new IllegalArgumentException("항목을 선택하거나 직접 입력해 주세요.");
//        }
//
//        if (request.getStartDate() == null) {
//            throw new IllegalArgumentException("결제 시작일은 필수입니다.");
//        }
//
//        if (!"1M".equals(request.getBillingCycle()) && !"1Y".equals(request.getBillingCycle())) {
//            throw new UniversityException(ExceptionMessage.INVALID_BILLING_CYCLE);
//        }
//
//        if (request.getCategoryId() == null) {
//            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
//        }
//
//        if (request.getPaymentId() == null) {
//            throw new IllegalArgumentException("결제수단을 선택해 주세요.");
//        }
//
//        if (subscriptionMapper.existsCategory(request.getCategoryId()) == 0) {
//            throw new UniversityException(ExceptionMessage.CATEGORY_NOT_FOUND);
//        }
//
//        if (subscriptionMapper.existsItem(request.getItemId()) == 0) {
//            throw new UniversityException(ExceptionMessage.ITEM_NOT_FOUND);
//        }
//
//        if (subscriptionMapper.existsItemInCategory(request.getCategoryId(), request.getItemId()) == 0) {
//            throw new UniversityException(ExceptionMessage.ITEM_CATEGORY_MISMATCH);
//        }
//
//        int duplicateCount = subscriptionMapper.countDuplicateSubscription(userId, request);
//        if (duplicateCount > 0) {
//            throw new UniversityException(ExceptionMessage.SUBSCRIPTION_DUPLICATE);
//        }
//
//        subscriptionMapper.insertSubscription(userId, request);
//        Long subscriptionId = subscriptionMapper.selectLastInsertedId();
//
//        return CreateSubscriptionResponse.builder()
//                .subscriptionId(subscriptionId)
//                .createdAt(LocalDate.now())
//                .build();
//    }
@Override
public CreateSubscriptionResponse createSubscription(Long userId, CreateSubscriptionRequest request) {
    if (request.getPrice() == null || request.getPrice() < 0) {
        throw new IllegalArgumentException("결제금액이 0원 이상이어야 합니다.");
    }

    if (request.getStartDate() == null) {
        throw new IllegalArgumentException("결제 시작일은 필수입니다.");
    }

    if (!"1M".equals(request.getBillingCycle()) && !"1Y".equals(request.getBillingCycle())) {
        throw new UniversityException(ExceptionMessage.INVALID_BILLING_CYCLE);
    }

    if (request.getCategoryId() == null) {
        throw new IllegalArgumentException("카테고리를 선택해 주세요.");
    }

    if (request.getPaymentId() == null) {
        throw new IllegalArgumentException("결제수단을 선택해 주세요.");
    }

    if (subscriptionMapper.existsCategory(request.getCategoryId()) == 0) {
        throw new UniversityException(ExceptionMessage.CATEGORY_NOT_FOUND);
    }
    boolean hasItemId = request.getItemId() != null;
    boolean hasItemName = request.getItemName() != null && !request.getItemName().trim().isEmpty();

    if (hasItemId && hasItemName) {
        throw new IllegalArgumentException("구독 항목은 선택하거나 직접 입력 중 하나만 가능합니다.");
    }

    if (subscriptionMapper.existsPaymentMethod(request.getPaymentId()) == 0) {
        throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
    }

    Long resolvedItemId = request.getItemId();

    // 기존 항목 선택도 안 했고, 직접입력도 안 한 경우
    if (resolvedItemId == null &&
            (request.getItemName() == null || request.getItemName().trim().isEmpty())) {
        throw new IllegalArgumentException("항목을 선택하거나 직접 입력해 주세요.");
    }

    // 직접입력인 경우
    if (resolvedItemId == null) {
        String trimmedItemName = request.getItemName().trim();

        Long existingItemId = subscriptionMapper.findItemIdByCategoryAndName(
                request.getCategoryId(), trimmedItemName
        );

        if (existingItemId != null) {
            resolvedItemId = existingItemId;
        } else {
            subscriptionMapper.insertSubscriptionItem(request.getCategoryId(), trimmedItemName);

            resolvedItemId = subscriptionMapper.findItemIdByCategoryAndName(
                    request.getCategoryId(), trimmedItemName
            );

            if (resolvedItemId == null) {
                throw new IllegalStateException("구독 항목 생성에 실패했습니다.");
            }
        }

        // 기존 insert / 중복검사 로직 그대로 쓰기 위해 itemId 세팅
        request.setItemId(resolvedItemId);
    }

    // 기존 항목 선택이든 직접입력이든 여기서부터는 itemId가 확정된 상태
    if (subscriptionMapper.existsItem(request.getItemId()) == 0) {
        throw new UniversityException(ExceptionMessage.ITEM_NOT_FOUND);
    }

    if (subscriptionMapper.existsItemInCategory(request.getCategoryId(), request.getItemId()) == 0) {
        throw new UniversityException(ExceptionMessage.ITEM_CATEGORY_MISMATCH);
    }

    int duplicateCount = subscriptionMapper.countDuplicateSubscription(userId, request);
    if (duplicateCount > 0) {
        throw new UniversityException(ExceptionMessage.SUBSCRIPTION_DUPLICATE);
    }

    subscriptionMapper.insertSubscription(userId, request);
    Long subscriptionId = subscriptionMapper.selectLastInsertedId();

    return CreateSubscriptionResponse.builder()
            .subscriptionId(subscriptionId)
            .createdAt(LocalDate.now())
            .build();
}

    @Override
    public SubscriptionResponse getSubscriptionById(Long userId, Long subscriptionId) {
        SubscriptionResponse response = subscriptionMapper.selectById(userId, subscriptionId);

        if (response == null) {
            throw new UniversityException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND);
        }

        return response;
    }

    @Override
    public UpdateSubscriptionResponse updateSubscription(Long userId, Long subscriptionId, UpdateSubscriptionRequest request) {
        Subscription existing = subscriptionMapper.selectSubscriptionById(userId, subscriptionId);
        if (existing == null) {
            throw new UniversityException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND);
        }

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("결제 금액은 0원 이상이어야 합니다.");
        }

        if (!"1M".equals(request.getBillingCycle()) && !"1Y".equals(request.getBillingCycle())) {
            throw new UniversityException(ExceptionMessage.INVALID_BILLING_CYCLE);
        }

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("다음 결제일은 필수입니다.");
        }

        if (request.getPaymentId() == null) {
            throw new IllegalArgumentException("결제 수단을 선택해 주세요.");
        }

        if (subscriptionMapper.existsPaymentMethod(request.getPaymentId()) == 0) {
            throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
        }

        if (subscriptionMapper.existsCategory(request.getCategoryId()) == 0) {
            throw new UniversityException(ExceptionMessage.CATEGORY_NOT_FOUND);
        }

        Long resolvedItemId = request.getItemId();

        if (resolvedItemId == null &&
                (request.getItemName() == null || request.getItemName().trim().isEmpty())) {
            throw new IllegalArgumentException("항목을 선택하거나 직접 입력해 주세요.");
        }

        if (resolvedItemId == null) {
            String trimmedItemName = request.getItemName().trim();

            Long existingItemId = subscriptionMapper.findItemIdByCategoryAndName(
                    request.getCategoryId(), trimmedItemName
            );

            if (existingItemId != null) {
                resolvedItemId = existingItemId;
            } else {
                subscriptionMapper.insertSubscriptionItem(request.getCategoryId(), trimmedItemName);

                resolvedItemId = subscriptionMapper.findItemIdByCategoryAndName(
                        request.getCategoryId(), trimmedItemName
                );

                if (resolvedItemId == null) {
                    throw new IllegalStateException("구독 항목 생성에 실패했습니다.");
                }
            }

            request.setItemId(resolvedItemId);
        }

        if (subscriptionMapper.existsItem(request.getItemId()) == 0) {
            throw new UniversityException(ExceptionMessage.ITEM_NOT_FOUND);
        }

        if (subscriptionMapper.existsItemInCategory(request.getCategoryId(), request.getItemId()) == 0) {
            throw new UniversityException(ExceptionMessage.ITEM_CATEGORY_MISMATCH);
        }

        boolean noChanges =
                Objects.equals(existing.getItemId(), request.getItemId()) &&
                        Objects.equals(existing.getPrice(), request.getPrice()) &&
                        Objects.equals(existing.getBillingCycle(), request.getBillingCycle()) &&
                        Objects.equals(existing.getStartDate(), request.getStartDate()) &&
                        Objects.equals(existing.getPaymentId(), request.getPaymentId());

        if (noChanges) {
            throw new UniversityException(ExceptionMessage.NO_CHANGES_DETECTED);
        }

        int result = subscriptionMapper.updateSubscription(userId, subscriptionId, request);

        if (result == 0) {
            throw new IllegalStateException("구독 수정에 실패했습니다.");
        }

        return UpdateSubscriptionResponse.builder()
                .subscriptionId(subscriptionId)
                .updatedAt(LocalDate.now())
                .build();
    }
//    @Override
//    public UpdateSubscriptionResponse updateSubscription(Long userId, Long subscriptionId, UpdateSubscriptionRequest request) {
//        Subscription existing = subscriptionMapper.selectSubscriptionById(userId, subscriptionId);
//        if (existing == null) {
//            throw new UniversityException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND);
//        }
//
//        if (request.getCategoryId() == null) {
//            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
//        }
//
//        if (request.getItemId() == null) {
//            throw new IllegalArgumentException("항목을 선택하거나 직접 입력해 주세요.");
//        }
//
//        if (request.getPrice() == null || request.getPrice() < 0) {
//            throw new IllegalArgumentException("결제 금액은 0원 이상이어야 합니다.");
//        }
//
//        if (!"1M".equals(request.getBillingCycle()) && !"1Y".equals(request.getBillingCycle())) {
//            throw new UniversityException(ExceptionMessage.INVALID_BILLING_CYCLE);
//        }
//
//        if (request.getStartDate() == null) {
//            throw new IllegalArgumentException("다음 결제일은 필수입니다.");
//        }
//
//        if (request.getPaymentId() == null) {
//            throw new IllegalArgumentException("결제 수단을 선택해 주세요.");
//        }
//
//        if (subscriptionMapper.existsPaymentMethod(request.getPaymentId()) == 0) {
//            throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
//        }
//
//        if (subscriptionMapper.existsCategory(request.getCategoryId()) == 0) {
//            throw new UniversityException(ExceptionMessage.CATEGORY_NOT_FOUND);
//        }
//
//        if (subscriptionMapper.existsItem(request.getItemId()) == 0) {
//            throw new UniversityException(ExceptionMessage.ITEM_NOT_FOUND);
//        }
//        boolean noChanges =
//                Objects.equals(existing.getItemId(), request.getItemId()) &&
//                        Objects.equals(existing.getPrice(), request.getPrice()) &&
//                        Objects.equals(existing.getBillingCycle(), request.getBillingCycle()) &&
//                        Objects.equals(existing.getStartDate(), request.getStartDate()) &&
//                        Objects.equals(existing.getPaymentId(), request.getPaymentId());
//
//        if (noChanges) {
//            throw new UniversityException(ExceptionMessage.NO_CHANGES_DETECTED);
//        }
//        if (subscriptionMapper.existsItemInCategory(request.getCategoryId(), request.getItemId()) == 0) {
//            throw new UniversityException(ExceptionMessage.ITEM_CATEGORY_MISMATCH);
//        }
//        int result = subscriptionMapper.updateSubscription(userId, subscriptionId, request);
//
//        if (result == 0) {
//            throw new IllegalStateException("구독 수정에 실패했습니다.");
//        }
//
//        return UpdateSubscriptionResponse.builder()
//                .subscriptionId(subscriptionId)
//                .updatedAt(LocalDate.now())
//                .build();
//    }

    @Override
    public void deleteSubscription(Long userId, Long subscriptionId) {
        int result = subscriptionMapper.softDeleteSubscription(userId, subscriptionId);

        if (result == 0) {
            throw new UniversityException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND);
        }
    }
}