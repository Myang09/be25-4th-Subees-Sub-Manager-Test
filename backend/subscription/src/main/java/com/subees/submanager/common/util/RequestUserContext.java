package com.subees.submanager.common.util;

import com.subees.submanager.common.exception.UniversityException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RequestUserContext {

    public Long requireUserId(Long userIdHeader) {
        if (userIdHeader == null) {
            throw new UniversityException("X-USER-ID 헤더가 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        return userIdHeader;
    }
}
