package com.subees.submanager.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetProfileImageResponse {
    private Long userId;
    private String profileImageUrl;
}