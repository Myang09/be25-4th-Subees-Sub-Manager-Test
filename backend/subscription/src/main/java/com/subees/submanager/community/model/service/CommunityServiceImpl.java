package com.subees.submanager.community.model.service;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.community.model.dto.post.CommunityPostCreateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostDetailResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostListResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostPageRequestDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapCreateDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapListResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapPageRequestDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapPageResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapResponseDto;
import com.subees.submanager.community.model.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;

    //글 목록 조회
    @Override
    public List<CommunityPostListResponseDto> getCommunityPostList(CommunityPostPageRequestDto communityPostPageRequestDto) {
        return communityMapper.selectCommunityPostList(communityPostPageRequestDto);
    }

    @Override
    public int getCommunityPostCount() {
        return communityMapper.selectCommunityPostCount();
    }

    //글 상세 조회 + 예외처리
    @Override
    public CommunityPostDetailResponseDto getCommunityPostDetail(long postId) {
        communityMapper.updateViewCount(postId); // 조회수 +1 먼저 실행
        CommunityPostDetailResponseDto post = communityMapper.selectCommunityPostDetail(postId);
        if (post == null) {
            throw new UniversityException(ExceptionMessage.POST_NOT_FOUND);
        }
        return post;
    }

    //글 작성
    @Transactional
    @Override
    public int save(CommunityPostCreateDto communityPostCreateDto) {
        // 미로그인 체크
        if (communityPostCreateDto.getUserId() == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        // title/content 유효성 체크
        if (communityPostCreateDto.getTitle() == null || communityPostCreateDto.getTitle().isBlank() ||
            communityPostCreateDto.getContent() == null || communityPostCreateDto.getContent().isBlank()) {
            throw new UniversityException(ExceptionMessage.INVALID_REQUEST);
        }
        return communityMapper.insertCommunityPost(communityPostCreateDto);
    }


    //글 수정
    @Transactional
    @Override
    public CommunityPostUpdateResponseDto update(CommunityPostUpdateDto communityPostUpdateDto) {
        // 미로그인 체크
        if (communityPostUpdateDto.getUserId() == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        // 게시글 존재 여부 및 작성자 조회
        Long ownerUserId = communityMapper.selectPostOwnerUserId(communityPostUpdateDto.getPostId());
        if (ownerUserId == null) {
            throw new UniversityException(ExceptionMessage.POST_NOT_FOUND);
        }
        // 권한 체크(작성자와 요청자가 다른지)
        if (!ownerUserId.equals(communityPostUpdateDto.getUserId())) {
            throw new UniversityException(ExceptionMessage.FORBIDDEN);
        }
        communityMapper.updateCommunityPost(communityPostUpdateDto); //dto에 담긴 값을 sql에 바인딩 하여 db 업데이트
        return communityMapper.selectUpdatedPost(communityPostUpdateDto.getPostId()); // 수정한 게시글을 db에서 다시 조회하여 CommunityPostUpdateResponseDto로 반환
    }

    //글 삭제
    @Override
    @Transactional
    public int delete(long postId, Long userId) {
        // 미로그인 체크
        if (userId == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        // 게시글 존재 여부 및 작성자 조회
        Long ownerUserId = communityMapper.selectPostOwnerUserId(postId);
        if (ownerUserId == null) {
            throw new UniversityException(ExceptionMessage.POST_NOT_FOUND);
        }
        // 권한 체크 (작성자와 요청자가 다른지)
        if (!ownerUserId.equals(userId)) {
            throw new UniversityException(ExceptionMessage.FORBIDDEN);
        }
        communityMapper.deleteAllScrapsByPostId(postId);
        return communityMapper.deleteCommunityPost(postId);
    }

    // 스크랩 저장
    @Transactional
    @Override
    public CommunityScrapResponseDto scrap(long postId, CommunityScrapCreateDto communityScrapCreateDto) {
        // 미로그인 체크
        if (communityScrapCreateDto.getUserId() == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        // 게시글 존재 여부 체크
        Long ownerUserId = communityMapper.selectPostOwnerUserId(postId);
        if (ownerUserId == null) {
            throw new UniversityException(ExceptionMessage.POST_NOT_FOUND);
        }
        // 자신의 글 스크랩 방지
        if (ownerUserId.equals(communityScrapCreateDto.getUserId())) {
            throw new UniversityException(ExceptionMessage.INVALID_SCRAP_REQUEST);
        }
        // 중복 스크랩 체크
        int duplicateCount = communityMapper.selectScrapDuplicateCount(postId, communityScrapCreateDto.getUserId());
        if (duplicateCount > 0) {
            throw new UniversityException(ExceptionMessage.ALREADY_SCRAPPED);
        }
        // postId 세팅 후 스크랩 저장
        communityScrapCreateDto.setPostId(postId);
        communityMapper.insertCommunityScrap(communityScrapCreateDto);
        // 게시글 scrap_count +1
        communityMapper.updateScrapCount(postId);
        // 저장된 스크랩 조회 후 반환
        return communityMapper.selectScrapById(communityScrapCreateDto.getScrapId());
    }

    // 스크랩 목록 조회 (페이징)
    @Override
    public CommunityScrapPageResponseDto getScrapList(Long userId, int page) {
        // 미로그인 체크
        if (userId == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        int size = 10;
        int totalCount = communityMapper.selectScrapTotalCount(userId);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        //페이징 예외처리
        if (page < 1 || (totalPages > 0 && page > totalPages)) {
            throw new UniversityException(ExceptionMessage.INVALID_PAGE);
        }

        CommunityScrapPageRequestDto requestDto = new CommunityScrapPageRequestDto(userId, page, size);
        List<CommunityScrapListResponseDto> scraps = communityMapper.selectScrapList(requestDto);

        return new CommunityScrapPageResponseDto(scraps, page, size, totalCount, totalPages);
    }

    // 스크랩 취소
    @Transactional
    @Override
    public void cancelScrap(long postId, Long userId) {
        // 미로그인 체크
        if (userId == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }
        // 스크랩 내역 존재 여부 체크
        int scrapCount = communityMapper.selectScrapDuplicateCount(postId, userId);
        if (scrapCount == 0) {
            throw new UniversityException(ExceptionMessage.SCRAP_NOT_FOUND);
        }
        // 스크랩 삭제 + scrap_count -1 (트랜잭션으로 묶임)
        communityMapper.deleteScrap(postId, userId);
        communityMapper.updateScrapCountDecrement(postId);
    }

}