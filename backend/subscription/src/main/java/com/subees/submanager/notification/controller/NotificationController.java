package com.subees.submanager.notification.controller;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.notification.model.dto.NotificationResponse;
import com.subees.submanager.notification.model.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        throw new UniversityException(ExceptionMessage.UNAUTHORIZED);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications() {
        Long userId = getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "알림 목록 조회 성공");
        response.put("data", notifications);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> readNotification(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();

        notificationService.readNotification(notificationId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "알림 읽음 처리 성공");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/close")
    public ResponseEntity<Map<String, Object>> closeNotification(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();

        notificationService.closeNotification(notificationId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "알림 닫기 처리 성공");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }
}