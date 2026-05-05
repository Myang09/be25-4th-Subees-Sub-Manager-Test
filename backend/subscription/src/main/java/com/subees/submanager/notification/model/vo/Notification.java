package com.subees.submanager.notification.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private Long notificationId;
    private Long userId;
    private Long subscriptionId;
    private String notifyType;   // D3, D0
    private String title;
    private String content;
    private LocalDate targetDate;
    private Integer isRead;
    private Integer isClosed;
    private LocalDateTime createdAt;
}