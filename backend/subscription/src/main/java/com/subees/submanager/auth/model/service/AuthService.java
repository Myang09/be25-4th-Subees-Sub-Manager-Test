package com.subees.submanager.auth.model.service;

import com.subees.submanager.auth.model.dto.LoginRequest;
import com.subees.submanager.auth.model.dto.LoginResponse;
import com.subees.submanager.auth.model.dto.LogoutResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LogoutResponse logout(Long tokenUserId);
}
