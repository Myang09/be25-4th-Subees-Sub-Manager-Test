package com.subees.submanager.community.model.mapper;

import com.subees.submanager.community.model.dto.post.CommunityPostCreateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostDetailResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostListResponseDto;
import com.subees.submanager.community.model.dto.post.CommunityPostPageRequestDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateDto;
import com.subees.submanager.community.model.dto.post.CommunityPostUpdateResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapCreateDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapListResponseDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapPageRequestDto;
import com.subees.submanager.community.model.dto.scrap.CommunityScrapResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// java code와 sql문을 연결하는 역할
@Mapper
public interface CommunityMapper {

    List<CommunityPostListResponseDto> selectCommunityPostList(CommunityPostPageRequestDto communityPostPageRequestDto); // 게시글 목록 조회 (페이징)

    int selectCommunityPostCount(); // 전체 게시글 수 조회

    CommunityPostDetailResponseDto selectCommunityPostDetail(@Param("postId") Long postId); // 게시글 상세 조회

    void updateViewCount(@Param("postId") Long postId); // 조회수 +1

    int insertCommunityPost(CommunityPostCreateDto communityPostCreateDto); //글 작성

    int updateCommunityPost(CommunityPostUpdateDto communityPostUpdateDto); //글 수정

    CommunityPostUpdateResponseDto selectUpdatedPost(@Param("postId") Long postId); //수정된 글 조회

    //수정 요청이 들어왔을 때, 해당 게시글의 작성자 user_id를 DB에서 가져오기 위해 추가
    Long selectPostOwnerUserId(@Param("postId") Long postId); //게시글 작성자 userId 조회

    int deleteCommunityPost(@Param("postId") Long postId); //게시글 삭제

    // 스크랩
    int insertCommunityScrap(CommunityScrapCreateDto communityScrapCreateDto); // 스크랩 저장
    CommunityScrapResponseDto selectScrapById(@Param("scrapId") Long scrapId); // 저장된 스크랩 조회
    int selectScrapDuplicateCount(@Param("postId") Long postId, @Param("userId") Long userId); // 중복 스크랩 체크
    void updateScrapCount(@Param("postId") Long postId); // 게시글 scrap_count +1

    // 스크랩 목록
    List<CommunityScrapListResponseDto> selectScrapList(CommunityScrapPageRequestDto communityScrapPageRequestDto); // 스크랩 목록 (페이징)
    int selectScrapTotalCount(@Param("userId") Long userId); // 스크랩 총 개수

    // 스크랩 취소
    int deleteScrap(@Param("postId") Long postId, @Param("userId") Long userId); // 스크랩 삭제
    void updateScrapCountDecrement(@Param("postId") Long postId); // scrap_count -1 (0 아래로 안 내려감)


    void deleteAllScrapsByPostId(long postId);

}