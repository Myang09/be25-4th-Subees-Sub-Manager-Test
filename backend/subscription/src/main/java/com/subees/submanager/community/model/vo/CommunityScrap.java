package com.subees.submanager.community.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityScrap {

    private long scrapId; //스크랩 id
    private long postId; // 게시글 id
    private long userId; // 사용자 id
    private LocalDateTime createdAt;
}
