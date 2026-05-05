package com.subees.submanager.community.model.service;

import com.subees.submanager.community.model.dto.post.CommunityPostCreateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostDetailResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostListResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostPageRequestDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapCreateDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapPageResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapResponseDto;

import java.util.List;

public interface CommunityService {

    // 게시글 목록 조회 (페이징)
    List<CommunityPostListResponseDto> getCommunityPostList(CommunityPostPageRequestDto communityPostPageRequestDto);

    // 전체 게시글 수
    int getCommunityPostCount();

    // 게시글 상세 조회 (조회수 +1 포함)
    CommunityPostDetailResponseDto getCommunityPostDetail(long postId);


    //글 작성
    int save(CommunityPostCreateDto communityPostCreateDto);

    //글 수정
    //int update
    CommunityPostUpdateResponseDto update(CommunityPostUpdateDto communityPostUpdateDto);

    //글 삭제
    int delete(long postId, Long userId);

    // 스크랩 저장
    CommunityScrapResponseDto scrap(long postId, CommunityScrapCreateDto communityScrapCreateDto);

    // 스크랩 목록 조회 (페이징)
    CommunityScrapPageResponseDto getScrapList(Long userId, int page);

    // 스크랩 취소
    void cancelScrap(long postId, Long userId);
}