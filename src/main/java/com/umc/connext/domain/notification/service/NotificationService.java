package com.umc.connext.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.umc.connext.domain.mynotification.service.MyNotificationService;
import com.umc.connext.domain.announcement.service.AnnouncementService;
import com.umc.connext.domain.notification.type.NotificationType;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MyNotificationService myNotificationService;
    private final AnnouncementService announcementService;

    public boolean hasUnreadNotification(Long memberId, NotificationType type) {
        return switch (type) {
            case NEWS -> myNotificationService.existsUnread(memberId);
            case NOTICES -> announcementService.existsUnread(memberId); // 수정필요
        };
    }
}
