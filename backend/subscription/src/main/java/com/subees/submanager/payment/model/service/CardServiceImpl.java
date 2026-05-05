package com.subees.submanager.payment.model.service;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.payment.model.dto.CardCreateRequestDto;
import com.subees.submanager.payment.model.dto.CardCreateResponseDto;
import com.subees.submanager.payment.model.dto.CardResponseDto;
import com.subees.submanager.payment.model.dto.CardUpdateRequestDto;
import com.subees.submanager.payment.model.mapper.CardMapper;
import com.subees.submanager.payment.model.vo.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    /*
     * 카드 등록
     * - 비로그인 시 등록 예외처리
     * - 카드 선택, 직접입력 둘 다 입력 or 둘 다 입력 X 예외처리
     * - 같은 카드 아이디 or 직접입력 카드, 별칭 등록할 시 예외처리
     */
    @Override
    public CardCreateResponseDto createCard(CardCreateRequestDto cardCreateRequestDto) {

        if (cardCreateRequestDto.getUserId() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        boolean hasCardId = cardCreateRequestDto.getCardId() != null;
        boolean hasCustomCardCompany = cardCreateRequestDto.getCustomCardCompany() != null
                && !cardCreateRequestDto.getCustomCardCompany().trim().isEmpty();

        if ((!hasCardId && !hasCustomCardCompany) || (hasCardId && hasCustomCardCompany)) {
            throw new UniversityException(ExceptionMessage.CARD_INPUT_INVALID);
        }

        if (hasCardId) {
            int duplicateCount = cardMapper.countCardId(
                    cardCreateRequestDto.getUserId(),
                    cardCreateRequestDto.getCardId(),
                    cardCreateRequestDto.getCardName()
            );
            if (duplicateCount > 0) {
                throw new UniversityException(ExceptionMessage.DUPLICATE_CARD);
            }
        }

        if (hasCustomCardCompany) {
            int duplicateCount = cardMapper.countCardCustom(
                    cardCreateRequestDto.getUserId(),
                    cardCreateRequestDto.getCustomCardCompany(),
                    cardCreateRequestDto.getCardName(),
                    cardCreateRequestDto.getIsActive()
            );
            if (duplicateCount > 0) {
                throw new UniversityException(ExceptionMessage.DUPLICATE_CARD);
            }
        }

        Payment payment = new Payment();
        payment.setUserId(cardCreateRequestDto.getUserId());
        payment.setCardId(hasCardId ? cardCreateRequestDto.getCardId() : null);
        payment.setCustomCardCompany(hasCustomCardCompany ? cardCreateRequestDto.getCustomCardCompany() : null);
        payment.setCardName(cardCreateRequestDto.getCardName());

        int result = cardMapper.insertPaymentMethod(payment);

        if (result != 1) {
            throw new UniversityException(ExceptionMessage.CARD_CREATE_FAILED);
        }

        return new CardCreateResponseDto(
                payment.getPaymentId(),
                payment.getCardName(),
                LocalDate.now()
        );
    }
    /*
     * 카드 수정
     * - 결제수단 ID,  사용자 ID, 카드 별칭X 예외처리
     * - 카드 선택 방식과 직접입력 방식 중 하나만 입력 가능
     * - 수정 중인 카드가 기존 카드와 중복 시(카드ID,직접입력카드,별칭,페이먼트ID) 예외처리
     */
    @Override
    public void updateCard(CardUpdateRequestDto request) {
        validateUpdateRequest(request);

        Payment existing = findExistingPayment(request.getPaymentId(), request.getUserId());

        CardType existingType = getExistingCardType(existing);
        CardType requestType = getRequestCardType(request);

        if (existingType != requestType) {
            throw new UniversityException(ExceptionMessage.CARD_TYPE_CHANGE_NOT_ALLOWED);
        }

        validateNoChanges(existing, request, existingType);
        validateDuplicate(request, existingType);

        Payment payment = new Payment();
        payment.setPaymentId(request.getPaymentId());
        payment.setUserId(request.getUserId());
        payment.setCardId(requestType == CardType.SELECTED ? request.getCardId() : null);
        payment.setCustomCardCompany(requestType == CardType.CUSTOM ? request.getCustomCardCompany() : null);
        payment.setCardName(request.getCardName());

        if (cardMapper.updateCard(payment) == 0) {
            throw new UniversityException(ExceptionMessage.CARD_UPDATE_FAILED);
        }
    }

    private void validateUpdateRequest(CardUpdateRequestDto request) {
        if (request.getPaymentId() == null) {
            throw new UniversityException(ExceptionMessage.PAYMENT_ID_REQUIRED);
        }
        if (request.getUserId() == null) {
            throw new UniversityException(ExceptionMessage.FORBIDDEN);
        }
        if (request.getCardName() == null || request.getCardName().trim().isEmpty()) {
            throw new UniversityException(ExceptionMessage.CARD_NAME_REQUIRED);
        }

        boolean hasCardId = request.getCardId() != null;
        boolean hasCustom = request.getCustomCardCompany() != null
                && !request.getCustomCardCompany().trim().isEmpty();

        if (hasCardId == hasCustom) {
            throw new UniversityException(ExceptionMessage.CARD_INPUT_INVALID);
        }
    }

    private Payment findExistingPayment(Long paymentId, Long userId) {
        Payment existing = cardMapper.selectPaymentById(paymentId);

        if (existing == null) {
            throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
        }
        if (existing.getUserId() != userId) {
            throw new UniversityException(ExceptionMessage.CARD_ACCESS_DENIED);
        }
        if (existing.getIsActive() == '0') {
            throw new UniversityException(ExceptionMessage.CARD_ALREADY_DELETED);
        }

        return existing;
    }

    private CardType getExistingCardType(Payment payment) {
        return payment.getCardId() != null ? CardType.SELECTED : CardType.CUSTOM;
    }

    private CardType getRequestCardType(CardUpdateRequestDto request) {
        return request.getCardId() != null ? CardType.SELECTED : CardType.CUSTOM;
    }

    private void validateNoChanges(Payment existing, CardUpdateRequestDto request, CardType type) {
        boolean same =
                type == CardType.SELECTED
                        ? existing.getCardId().equals(request.getCardId())
                        && existing.getCardName().equals(request.getCardName())
                        : existing.getCustomCardCompany().equals(request.getCustomCardCompany())
                        && existing.getCardName().equals(request.getCardName());

        if (same) {
            throw new UniversityException(ExceptionMessage.NO_CHANGES_DETECTED);
        }
    }

    private void validateDuplicate(CardUpdateRequestDto request, CardType type) {
        int duplicateCount =
                type == CardType.SELECTED
                        ? cardMapper.duplicateSelectedCard(
                        request.getUserId(),
                        request.getCardId(),
                        request.getCardName(),
                        request.getPaymentId()
                )
                        : cardMapper.duplicateCustomCard(
                        request.getUserId(),
                        request.getCustomCardCompany(),
                        request.getCardName(),
                        request.getPaymentId()
                );

        if (duplicateCount > 0) {
            throw new UniversityException(ExceptionMessage.DUPLICATE_CARD);
        }
    }

    private enum CardType {
        SELECTED, CUSTOM
    }

    /*
     * 카드 삭제
     */
    @Override
    public void deleteCard(Long paymentId, Long userId) {

        if (userId == null) {
            throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
        }

        if (paymentId == null) {
            throw new UniversityException(ExceptionMessage.PAYMENT_ID_REQUIRED);
        }

        Payment payment = cardMapper.selectPaymentById(paymentId);

        if (payment == null) {
            throw new UniversityException(ExceptionMessage.PAYMENT_METHOD_NOT_FOUND);
        }

        if (payment.getUserId() != userId) {
            throw new UniversityException(ExceptionMessage.CARD_ACCESS_DENIED);
        }

        if (payment.getIsActive() == '0') {
            throw new UniversityException(ExceptionMessage.CARD_ALREADY_DELETED);
        }

        int usingCount = cardMapper.activeCardByPayment(paymentId);
        if (usingCount > 0) {
            throw new UniversityException(ExceptionMessage.CARD_IN_USE);
        }

        int result = cardMapper.deleteCard(userId, paymentId);

        if (result != 1) {
            throw new UniversityException(ExceptionMessage.CARD_DELETE_FAILED);
        }
    }

    /*
    * 카드 조회
     */
    @Override
    public List<CardResponseDto> getCards(Long userId) {

        if (userId == null) {
            throw new UniversityException(ExceptionMessage.USER_NOT_FOUND);
        }


        List<Payment> paymentList = cardMapper.selectCards(userId);

        return paymentList.stream()
                .map(payment -> new CardResponseDto(
                        payment.getPaymentId(),
                        payment.getCardId(),
                        payment.getCardCompany(),
                        payment.getCardName(),
                        payment.getCustomCardCompany(),
                        payment.getIsActive()
                ))
                .toList();
    }

}