package com.subees.submanager.community.model.dto.post;
//페이징 응답용

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor //필드 전부를 한 번에 넣어서 객체 만들 수 있게 생성자 자동 생성

//게시글 데이터 + 페이징 정보 응답용
public class CommunityPostPageResponseDto {

    private List<CommunityPostListResponseDto> posts; // 게시글 데이터

    //페이지 정보
    private int size;
    private int page;
    private int totalCount;
    private int totalPages;

}
