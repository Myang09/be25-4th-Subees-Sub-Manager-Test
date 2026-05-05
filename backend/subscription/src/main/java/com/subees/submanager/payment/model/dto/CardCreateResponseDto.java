package com.subees.submanager.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CardCreateResponseDto {
    private Long paymentId;
    private String cardName;
    private LocalDate createdAt;
}