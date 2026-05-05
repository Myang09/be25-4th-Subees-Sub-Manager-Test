package com.subees.submanager.payment.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class Payment {
    private Long cardId; // 카드 아이디

    private long userId; // 유저 아아디

    public String cardName; // 카드 별칭

    private String cardCompany; // 카드사 이름

    private Long paymentId; // paymentMethod pk

    private char isActive; // 카드 활성화 여부

    private LocalDate created_at; // 카드 등록 시간

    private String customCardCompany; // 유저가 직접입력한 카드 이름
}
