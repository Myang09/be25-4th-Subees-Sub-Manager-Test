package com.subees.submanager.community.model.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 스크랩 목록 조회 응답 (한 건)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityScrapListResponseDto {

    private Long scrapId;
    private Long postId;
    private String title;
    private String nickname; // 게시글 작성자 닉네임
    private LocalDateTime createdAt; // 스크랩 저장일
}