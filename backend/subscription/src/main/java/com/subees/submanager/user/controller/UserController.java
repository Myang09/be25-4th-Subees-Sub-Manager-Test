package com.subees.submanager.user.controller;

import com.subees.submanager.common.model.dto.BaseResponseDto;
import com.subees.submanager.user.model.dto.*;
import com.subees.submanager.user.model.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<BaseResponseDto<String>> signUp(@RequestBody @Valid SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseDto<>(HttpStatus.CREATED, "회원가입이 완료되었습니다."));
    }

    @GetMapping("/check-email")
    public ResponseEntity<BaseResponseDto<CheckEmailResponse>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "이메일 중복 확인 성공", userService.checkEmail(email))
        );
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<BaseResponseDto<CheckNicknameResponse>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "닉네임 중복 확인 성공", userService.checkNickname(nickname))
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponseDto<MyInfoResponse>> getMyInfo(@PathVariable Long userId,
                                                                     Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "내 정보 조회 성공", userService.getMyInfo(userId, tokenUserId))
        );
    }

    @PatchMapping("/{userId}/profile")
    public ResponseEntity<BaseResponseDto<UpdateProfileResponse>> updateProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long tokenUserId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UpdateProfileResponse response = userService.updateProfile(userId, tokenUserId, request);

        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "프로필이 수정되었습니다.", response)
        );
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<BaseResponseDto<ChangePasswordResponse>> changePassword(@PathVariable Long userId,
                                                                                  @RequestBody @Valid ChangePasswordRequest request,
                                                                                  Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "비밀번호가 변경되었습니다.",
                        userService.changePassword(userId, tokenUserId, request))
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<BaseResponseDto<WithdrawResponse>> withdraw(@PathVariable Long userId,
                                                                      Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "회원 탈퇴가 완료되었습니다.",
                        userService.withdraw(userId, tokenUserId))
        );
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<BaseResponseDto<GetProfileImageResponse>> getProfileImage(@PathVariable Long userId,
                                                                                    Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "프로필 이미지 조회 성공",
                        userService.getProfileImage(userId, tokenUserId))
        );
    }

    @PatchMapping("/{userId}/profile-image")
    public ResponseEntity<BaseResponseDto<UpdateProfileImageResponse>> updateProfileImage(@PathVariable Long userId,
                                                                                          @RequestBody @Valid UpdateProfileImageRequest request,
                                                                                          Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "프로필 이미지가 수정되었습니다.",
                        userService.updateProfileImage(userId, tokenUserId, request))
        );
    }

    @DeleteMapping("/{userId}/profile-image")
    public ResponseEntity<BaseResponseDto<DeleteProfileImageResponse>> deleteProfileImage(@PathVariable Long userId,
                                                                                          Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "프로필 이미지가 삭제되었습니다.",
                        userService.deleteProfileImage(userId, tokenUserId))
        );
    }

    @PatchMapping("/{userId}/profile-image/file")
    public ResponseEntity<BaseResponseDto<UploadProfileImageResponse>> uploadProfileImageFile(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new BaseResponseDto<>(HttpStatus.OK, "프로필 이미지가 업로드되었습니다.",
                        userService.uploadProfileImageFile(userId, tokenUserId, file))
        );
    }
}