package com.subees.submanager.community.model.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 스크랩 목록 페이징 응답
@Getter
@AllArgsConstructor
public class CommunityScrapPageResponseDto {

    private List<CommunityScrapListResponseDto> scraps;
    private int page;
    private int size;
    private int totalCount;
    private int totalPages;
}