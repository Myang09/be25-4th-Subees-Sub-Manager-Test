package com.subees.submanager.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDto {
    private Long paymentId;
    private Long cardId;
    private String cardCompany;
    private String cardName;
    private String customCardCompany;
    private char isActive;
}
