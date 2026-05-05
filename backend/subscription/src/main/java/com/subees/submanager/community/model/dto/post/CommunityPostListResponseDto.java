package com.subees.submanager.community.model.dto.post;
//화면에 필요한 데이터 전달용. userId 대신 nickname으로 가져와볼것
//글 목록 Dto

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter //resultMap의 property에 값 주입 시 사용
@NoArgsConstructor //mybatis가 객체 생성시 사용
public class CommunityPostListResponseDto {

    private Long postId;
    private String title;
    private String nickname;
    private LocalDateTime createdAt;
    private int viewCount;

}
