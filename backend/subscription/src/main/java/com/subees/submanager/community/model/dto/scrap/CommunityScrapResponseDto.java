package com.subees.submanager.community.model.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 스크랩 생성 응답
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityScrapResponseDto {

    private Long scrapId;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}