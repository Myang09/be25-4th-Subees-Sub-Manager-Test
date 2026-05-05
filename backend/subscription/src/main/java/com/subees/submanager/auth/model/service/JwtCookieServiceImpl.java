package com.subees.submanager.auth.model.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieServiceImpl implements JwtCookieService {

    private static final String COOKIE_PATH = "/";
    private static final long COOKIE_MAX_AGE_SECONDS = 60L * 60L * 24L;

    @Override
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();
    }

    @Override
    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
