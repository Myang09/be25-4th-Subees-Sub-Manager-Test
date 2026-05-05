package com.subees.submanager.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateProfileImageRequest {

    @NotBlank(message = "프로필 이미지 URL은 비어 있을 수 없습니다.")
    private String profileImageUrl;
}