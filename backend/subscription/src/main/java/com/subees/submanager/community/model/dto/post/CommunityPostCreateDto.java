package com.subees.submanager.community.model.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter //resultMap의 property에 값 주입 시 사용
@NoArgsConstructor //mybatis가 객체 생성시 사용
public class CommunityPostCreateDto {

    private Long postId;

    private Long userId;

    private String title;

    private String content;

}
