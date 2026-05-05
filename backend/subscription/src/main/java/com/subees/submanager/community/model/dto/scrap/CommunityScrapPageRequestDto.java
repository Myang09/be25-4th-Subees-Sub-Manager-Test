package com.subees.submanager.community.model.dto.scrap;

import lombok.Getter;

// 스크랩 목록 페이징 요청
@Getter
public class CommunityScrapPageRequestDto {

    private final Long userId;
    private final int page;
    private final int size;

    public CommunityScrapPageRequestDto(Long userId, int page, int size) {
        this.userId = userId;
        this.page = (page < 1) ? 1 : page;
        this.size = size;
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}