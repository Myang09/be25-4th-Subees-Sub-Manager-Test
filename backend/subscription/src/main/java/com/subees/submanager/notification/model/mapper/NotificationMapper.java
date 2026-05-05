package com.subees.submanager.notification.model.mapper;

import com.subees.submanager.notification.model.dto.NotificationResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    List<NotificationResponse> selectNotificationsByUserId(@Param("userId") Long userId);

    void updateNotificationRead(@Param("notificationId") Long notificationId);

    void updateNotificationClose(@Param("notificationId") Long notificationId);

    void insertNotifications(@Param("userId") Long userId);

    int existsReadableNotification(@Param("notificationId") Long notificationId);

    int existsClosableNotification(@Param("notificationId") Long notificationId);

    
}