package com.subees.submanager.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateProfileResponse {

    private Long userId;
    private String email;
    private String nickname;
}