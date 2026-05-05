package com.subees.submanager.auth.controller;

import com.subees.submanager.auth.model.dto.LoginRequest;
import com.subees.submanager.auth.model.dto.LoginResponse;
import com.subees.submanager.auth.model.dto.LogoutResponse;
import com.subees.submanager.auth.model.service.AuthService;
import com.subees.submanager.auth.model.service.JwtCookieService;
import com.subees.submanager.common.model.dto.BaseResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDto<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        ResponseCookie accessTokenCookie = jwtCookieService.createAccessTokenCookie(response.getAccessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(new BaseResponseDto<>(HttpStatus.OK, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponseDto<LogoutResponse>> logout(Authentication authentication) {
        Long tokenUserId = (Long) authentication.getPrincipal();
        LogoutResponse response = authService.logout(tokenUserId);
        ResponseCookie clearCookie = jwtCookieService.clearAccessTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(new BaseResponseDto<>(HttpStatus.OK, "로그아웃이 완료되었습니다.", response));
    }
}
