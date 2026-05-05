package com.subees.submanager.notification.model.service;

import com.subees.submanager.notification.model.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Long userId);

    void readNotification(Long notificationId);

    void closeNotification(Long notificationId);

    void generateNotifications(Long userId);
}