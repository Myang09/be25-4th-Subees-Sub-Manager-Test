package com.subees.submanager.community.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//@Data // getter, setter, toString, equals, hashcode 자동 생성해줌
@Builder //가독성 좋게 생성자를 생성해주며 파라미터 순서에 헷갈릴 일이 없음
@NoArgsConstructor //빈 객체를 만들 기본 생성자 생성을 도와줌.  없으면 에러남
@AllArgsConstructor //모든 필드를 매개변수로 한번에 받는 생성자를 만들어줌. @Builder를 사용하려면 모든 필드를 포함하는 생성자가 있어야 함

public class CommunityPost {

    private long postId; // 게시글 id
    private long userId; // 유저 id fk
    private String title; // 제목
    private String content; // 내용
    private Integer viewCount; //조회수 0
    private Integer scrapCount; // 스크랩 수 0
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
