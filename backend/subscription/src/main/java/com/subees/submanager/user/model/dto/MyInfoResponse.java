package com.subees.submanager.user.model.dto;

import com.subees.submanager.user.model.vo.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyInfoResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String userState;
    private String emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;

    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getUserState().name(),
                user.getEmailVerified(),
                user.getEmailVerifiedAt(),
                user.getCreatedAt()
        );
    }
}