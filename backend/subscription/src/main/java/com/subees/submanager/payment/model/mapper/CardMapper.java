package com.subees.submanager.payment.model.mapper;

import com.subees.submanager.payment.model.vo.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardMapper {
    int insertPaymentMethod(Payment payment);

    int countCardCustom(@Param("userId") Long userId, @Param("customCardCompany") String customCardCompany, @Param("cardName") String cardName, @Param("is_active") char isActive);

    int countCardId(@Param("userId") Long userId, @Param("cardId") Long cardId, @Param("cardName") String cardName);

    int updateCard(Payment payment);

    Payment selectPaymentById(Long paymentId);

    int activeCardByPayment(@Param("paymentId") Long paymentId);

    int deleteCard(@Param("userId") Long userId, @Param("paymentId") Long paymentId);

    int duplicateSelectedCard(
            @Param("userId") Long userId,
            @Param("cardId") Long cardId,
            @Param("cardName") String cardName,
            @Param("paymentId") Long paymentId
    );

    int duplicateCustomCard(
            @Param("userId") Long userId,
            @Param("customCardCompany") String customCardCompany,
            @Param("cardName") String cardName,
            @Param("paymentId") Long paymentId
    );

    List<Payment> selectCards(@Param("userId") Long userId);
}
