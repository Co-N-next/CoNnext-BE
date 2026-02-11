package com.umc.connext.domain.mynotification.service;

import com.umc.connext.domain.mynotification.entity.*;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateShareService {

    private final MyNotificationRepository myNotificationRepository;

    @Transactional
    public void accept(Long memberId, Long notificationId) {

        MyNotification notification =
                myNotificationRepository
                        .findByMyNotificationIdAndMemberId(notificationId, memberId)
                        .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (notification.getCategory() != Category.MATE) {
            throw new RuntimeException("메이트 요청 알림이 아님");
        }

        if (notification.getActionType() != ActionType.ACCEPT_REJECT) {
            throw new RuntimeException("수락 가능한 알림이 아님");
        }

        if (notification.getActionStatus() != ActionStatus.PENDING) {
            throw new RuntimeException("이미 처리된 알림");
        }

        notification.accept();
    }

    @Transactional
    public void reject(Long memberId, Long notificationId) {

        MyNotification notification =
                myNotificationRepository
                        .findByMyNotificationIdAndMemberId(notificationId, memberId)
                        .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (notification.getCategory() != Category.MATE) {
            throw new RuntimeException("메이트 요청 알림이 아님");
        }

        if (notification.getActionStatus() != ActionStatus.PENDING) {
            throw new RuntimeException("이미 처리된 알림");
        }

        notification.reject();
    }
}
