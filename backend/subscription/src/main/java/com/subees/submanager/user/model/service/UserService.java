package com.subees.submanager.user.model.service;

import com.subees.submanager.user.model.dto.ChangePasswordRequest;
import com.subees.submanager.user.model.dto.ChangePasswordResponse;
import com.subees.submanager.user.model.dto.CheckEmailResponse;
import com.subees.submanager.user.model.dto.CheckNicknameResponse;
import com.subees.submanager.user.model.dto.DeleteProfileImageResponse;
import com.subees.submanager.user.model.dto.GetProfileImageResponse;
import com.subees.submanager.user.model.dto.MyInfoResponse;
import com.subees.submanager.user.model.dto.SignUpRequest;
import com.subees.submanager.user.model.dto.UpdateProfileImageRequest;
import com.subees.submanager.user.model.dto.UpdateProfileImageResponse;
import com.subees.submanager.user.model.dto.UpdateProfileRequest;
import com.subees.submanager.user.model.dto.UpdateProfileResponse;
import com.subees.submanager.user.model.dto.UploadProfileImageResponse;
import com.subees.submanager.user.model.dto.WithdrawResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void signUp(SignUpRequest request);
    CheckEmailResponse checkEmail(String email);
    CheckNicknameResponse checkNickname(String nickname);
    MyInfoResponse getMyInfo(Long pathUserId, Long tokenUserId);
    UpdateProfileResponse updateProfile(Long pathUserId, Long tokenUserId, UpdateProfileRequest request);
    ChangePasswordResponse changePassword(Long pathUserId, Long tokenUserId, ChangePasswordRequest request);
    WithdrawResponse withdraw(Long pathUserId, Long tokenUserId);
    GetProfileImageResponse getProfileImage(Long pathUserId, Long tokenUserId);
    UpdateProfileImageResponse updateProfileImage(Long pathUserId, Long tokenUserId, UpdateProfileImageRequest request);
    DeleteProfileImageResponse deleteProfileImage(Long pathUserId, Long tokenUserId);
    UploadProfileImageResponse uploadProfileImageFile(Long pathUserId, Long tokenUserId, MultipartFile file);
}
