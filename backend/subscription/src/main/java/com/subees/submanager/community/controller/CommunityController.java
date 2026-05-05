package com.subees.submanager.community.controller;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostCreateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostDetailResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostListResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostPageRequestDto;
import com.subees.submanager.community.model.dto.post.CommunityPostPageResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapCreateDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapPageResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapResponseDto;
import com.subees.submanager.community.model.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 현재 로그인한 사용자 userId 가져오기
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
    }

    // 게시글 목록 조회
    @GetMapping("/community/posts")
    public ResponseEntity<BaseResponseDto<CommunityPostPageResponseDto>> getCommunityPostList(
            @RequestParam(defaultValue = "1") int page) {

        int size = 10;

        int totalCount = communityService.getCommunityPostCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        if (page < 1 || (totalPages > 0 && page > totalPages)) {
            throw new UniversityException(ExceptionMessage.INVALID_PAGE);
        }

        CommunityPostPageRequestDto communityPostPageRequestDto = new CommunityPostPageRequestDto(page, size);
        List<CommunityPostListResponseDto> posts =
                communityService.getCommunityPostList(communityPostPageRequestDto);

        CommunityPostPageResponseDto responseDto = new CommunityPostPageResponseDto(
                posts,
                communityPostPageRequestDto.getPage(),
                communityPostPageRequestDto.getSize(),
                totalCount,
                totalPages
        );

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, responseDto));
    }

    // 게시글 상세 조회
    @GetMapping("/community/posts/{postId}")
    public ResponseEntity<BaseResponseDto<CommunityPostDetailResponseDto>> getCommunityPostDetail(
            @PathVariable long postId) {

        CommunityPostDetailResponseDto post = communityService.getCommunityPostDetail(postId);
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, post));
    }

    // 게시글 작성
    @PostMapping("/community/posts")
    public ResponseEntity<BaseResponseDto<CommunityPostCreateDto>> postCommunityCreate(
            @RequestBody CommunityPostCreateDto communityPostCreateDto) {

        Long userId = getCurrentUserId();
        communityPostCreateDto.setUserId(userId);

        communityService.save(communityPostCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, communityPostCreateDto));
    }

    // 게시글 수정
    @PutMapping("/community/posts/{postId}")
    public ResponseEntity<BaseResponseDto<CommunityPostUpdateResponseDto>> postCommunityUpdate(
            @PathVariable long postId,
            @Valid @RequestBody CommunityPostUpdateDto communityPostUpdateDto) {

        Long userId = getCurrentUserId();

        communityPostUpdateDto.setPostId(postId);
        communityPostUpdateDto.setUserId(userId);

        CommunityPostUpdateResponseDto updatedPost = communityService.update(communityPostUpdateDto);

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, updatedPost));
    }

    // 게시글 삭제
    @DeleteMapping("/community/posts/{postId}")
    public ResponseEntity<BaseResponseDto<Long>> postCommunityDelete(@PathVariable long postId) {
        Long userId = getCurrentUserId();
        communityService.delete(postId, userId);
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, postId));
    }

    // 게시글 스크랩
    @PostMapping("/community/posts/{postId}/scraps")
    public ResponseEntity<BaseResponseDto<CommunityScrapResponseDto>> postCommunityScrap(
            @PathVariable long postId) {

        Long userId = getCurrentUserId();

        CommunityScrapCreateDto communityScrapCreateDto = new CommunityScrapCreateDto();
        communityScrapCreateDto.setUserId(userId);

        CommunityScrapResponseDto scrap = communityService.scrap(postId, communityScrapCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, scrap));
    }

    // 스크랩 목록 조회
    @GetMapping("/community/scraps")
    public ResponseEntity<BaseResponseDto<CommunityScrapPageResponseDto>> getScrapList(
            @RequestParam(defaultValue = "1") int page) {

        Long userId = getCurrentUserId();
        CommunityScrapPageResponseDto response = communityService.getScrapList(userId, page);

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, response));
    }

    // 스크랩 취소
    @DeleteMapping("/community/posts/{postId}/scraps")
    public ResponseEntity<BaseResponseDto<Long>> cancelScrap(@PathVariable long postId) {
        Long userId = getCurrentUserId();
        communityService.cancelScrap(postId, userId);
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, postId));
    }
}