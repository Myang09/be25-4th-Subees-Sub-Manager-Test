package com.subees.submanager.auth.model.service;

import com.subees.submanager.auth.model.dto.LoginRequest;
import com.subees.submanager.auth.model.dto.LoginResponse;
import com.subees.submanager.auth.model.dto.LogoutResponse;
import com.subees.submanager.auth.jwt.JwtTokenProvider;
import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.user.model.vo.User;
import com.subees.submanager.user.model.vo.UserState;
import com.subees.submanager.user.model.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new UniversityException(ExceptionMessage.INVALID_EMAIL);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UniversityException(ExceptionMessage.INVALID_PASSWORD);
        }
        if (user.getUserState() == UserState.INACTIVE) {
            throw new UniversityException(ExceptionMessage.WITHDRAWN_USER);
        }
        if (user.getUserState() != UserState.ACTIVE) {
            throw new UniversityException(ExceptionMessage.INACTIVE_USER);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getNickname());
        return new LoginResponse("Bearer", accessToken, user.getUserId(), user.getNickname());
    }

    @Override
    public LogoutResponse logout(Long tokenUserId) {
        return new LogoutResponse(tokenUserId);
    }
}
