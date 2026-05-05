package com.subees.submanager.community.model.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor

//수정 요청
public class CommunityPostUpdateDto {

    private Long postId;
    private Long userId; // 401 체크용(요청자 식별용. 로그인 여부 + 권한 체크용)

    //@NotBlank ("", null, "   " 거부)
    @NotBlank(message = "제목 입력은 필수입니다")
    private String title;

    @NotBlank(message = "내용 입력은 필수입니다")
    private String content;

}
