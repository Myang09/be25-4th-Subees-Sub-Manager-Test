package com.subees.submanager.payment.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CardCreateRequestDto {
    private Long userId; //유저 아이디

    private Long cardId; // 기본제공 카드 아이디

    private String customCardCompany; // 유저가 직접 입력한 카드 이름

    private String cardName; // 카드 이름

    private char isActive;


}
