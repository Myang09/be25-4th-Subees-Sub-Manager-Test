package com.subees.submanager.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String grantType;
    private String accessToken;
    private Long userId;
    private String nickname;
}