package com.subees.submanager.notification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String title;
    private String content;
    private String dday;     // D-3, D-DAY
    private String isRead;   // Y/N or 0/1 그대로 써도 됨
}