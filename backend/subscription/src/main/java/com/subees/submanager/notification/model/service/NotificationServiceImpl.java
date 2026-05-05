package com.subees.submanager.notification.model.service;

import com.subees.submanager.common.exception.UniversityException;
import com.subees.submanager.common.exception.message.ExceptionMessage;
import com.subees.submanager.notification.model.dto.NotificationResponse;
import com.subees.submanager.notification.model.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements com.subees.submanager.notification.model.service.NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponse> getNotifications(Long userId) {
        notificationMapper.insertNotifications(userId);
        return notificationMapper.selectNotificationsByUserId(userId);
    }

    @Override
    public void readNotification(Long notificationId) {
        int exists = notificationMapper.existsReadableNotification(notificationId);

        if (exists == 0) {
            throw new UniversityException(ExceptionMessage.NOTIFICATION_NOT_FOUND);
        }

        notificationMapper.updateNotificationRead(notificationId);
    }

    @Override
    public void closeNotification(Long notificationId) {
        int exists = notificationMapper.existsClosableNotification(notificationId);

        if (exists == 0) {
            throw new UniversityException(ExceptionMessage.NOTIFICATION_NOT_FOUND);
        }

        notificationMapper.updateNotificationClose(notificationId);
    }

    @Override
    public void generateNotifications(Long userId) {
        notificationMapper.insertNotifications(userId);
    }
}