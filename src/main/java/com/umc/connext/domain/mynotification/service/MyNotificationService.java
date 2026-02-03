package com.umc.connext.domain.mynotification.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.umc.connext.common.response.PageInfo;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPageResponse;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationResponseDTO;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPayload;

@Service
@RequiredArgsConstructor
public class MyNotificationService {
    private final MyNotificationRepository myNotificationRepository;

    public boolean existsUnread() {
        return myNotificationRepository.existsByIsReadFalse();
    }

    public MyNotificationPageResponse getMyNotifications(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 수정 필요
        Long memberId = 1L;

        Page<MyNotification> result =
                myNotificationRepository.findAllByMemberId(memberId, pageable);

        List<MyNotificationResponseDTO> news = result.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        PageInfo pageInfo = PageInfo.of(result);

        return new MyNotificationPageResponse(
                pageInfo,
                new MyNotificationPayload(news)
        );
    }

    private MyNotificationResponseDTO toDto(MyNotification entity) {
        return MyNotificationResponseDTO.builder()
                .id(entity.getMyNotificationId())
                .senderProfileImg(entity.getImg())
                .senderId(entity.getSenderId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.getIsRead())
                .category(entity.getCategory())
                .actionType(entity.getActionType())
                .actionStatus(entity.getActionStatus())
                .img(entity.getImg())
                .build();
    }
}
