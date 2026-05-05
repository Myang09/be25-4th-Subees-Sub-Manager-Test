package com.subees.submanager.payment.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.payment.model.dto.CardCreateRequestDto;
import com.subees.submanager.payment.model.dto.CardCreateResponseDto;
import com.subees.submanager.payment.model.dto.CardResponseDto;
import com.subees.submanager.payment.model.dto.CardUpdateRequestDto;
import com.subees.submanager.payment.model.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentController {

    private final CardService cardService;

    // 카드 등록
    @PostMapping
    public ResponseEntity<BaseResponseDto<CardCreateResponseDto>> createCard(
            Authentication authentication,
            @RequestBody CardCreateRequestDto dto
    ) {
        Long userId = (Long) authentication.getPrincipal();
        dto.setUserId(userId);

        CardCreateResponseDto result = cardService.createCard(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, "카드 등록을 완료하였습니다.", result));
    }

    // 카드 수정
    @PatchMapping("/{paymentId}")
    public ResponseEntity<BaseResponseDto<String>> updateCard(
            Authentication authentication,
            @PathVariable Long paymentId,
            @RequestBody CardUpdateRequestDto requestDto
    ) {
        Long userId = (Long) authentication.getPrincipal();

        requestDto.setPaymentId(paymentId);
        requestDto.setUserId(userId);

        cardService.updateCard(requestDto);

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, "카드 수정 성공"));
    }

    // 카드 삭제
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<BaseResponseDto<String>> deleteCard(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        cardService.deleteCard(paymentId, userId);

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, "카드를 삭제하였습니다."));
    }

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<CardResponseDto>>> getCards(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                new BaseResponseDto<>(
                        HttpStatus.OK,
                        cardService.getCards((Long) authentication.getPrincipal())
                )
        );
    }
}