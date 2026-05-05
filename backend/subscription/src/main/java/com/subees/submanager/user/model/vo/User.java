package com.subees.submanager.user.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private Long userId;
    private String email;
    private String password;
    private String nickname;
    private UserState userState;
    private String emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private String profileImageUrl;

    @Builder
    public User(String email, String password, String nickname, UserState userState,
                String emailVerified, LocalDateTime emailVerifiedAt, LocalDateTime createdAt) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userState = userState;
        this.emailVerified = emailVerified;
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateUserState(UserState userState) {
        this.userState = userState;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}