package com.umc.connext.domain.mynotification.service;

import com.umc.connext.domain.mynotification.entity.ActionStatus;
import com.umc.connext.domain.mynotification.entity.ActionType;
import com.umc.connext.domain.mynotification.entity.Category;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationShareService {

    private final MyNotificationRepository myNotificationRepository;

    @Transactional
    public void accept(Long memberId, Long notificationId) {

        MyNotification notification =
                myNotificationRepository
                        .findByMyNotificationIdAndMemberId(notificationId, memberId)
                        .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (notification.getCategory() != Category.LOCATION) {
            throw new RuntimeException("위치 공유 알림이 아님");
        }

        if (notification.getActionType() != ActionType.ACCEPT_REJECT) {
            throw new RuntimeException("수락 가능한 알림이 아님");
        }

        if (notification.getActionStatus() != ActionStatus.PENDING) {
            throw new RuntimeException("이미 처리된 알림");
        }

        notification.accept();
    }

}
