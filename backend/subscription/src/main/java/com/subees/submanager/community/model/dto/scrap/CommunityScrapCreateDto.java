package com.subees.submanager.community.model.dto.scrap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 스크랩 요청
@Getter
@Setter
@NoArgsConstructor
public class CommunityScrapCreateDto {

    private Long userId;  // 요청자 (로그인 여부 + 권한 체크용)
    private Long postId;  // PathVariable에서 세팅
    private Long scrapId; // INSERT 후 useGeneratedKeys로 채워짐
}
