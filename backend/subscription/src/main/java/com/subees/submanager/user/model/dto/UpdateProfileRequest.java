package com.subees.submanager.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}