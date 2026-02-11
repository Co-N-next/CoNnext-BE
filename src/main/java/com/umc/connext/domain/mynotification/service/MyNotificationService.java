package com.umc.connext.domain.mynotification.service;

import java.util.List;
import com.umc.connext.domain.mynotification.service.LocationShareService;
import com.umc.connext.domain.mynotification.service.MateShareService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.umc.connext.domain.mynotification.entity.ActionType;
import com.umc.connext.domain.mynotification.entity.ActionStatus;
import com.umc.connext.common.response.PageInfo;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import com.umc.connext.domain.mynotification.entity.Category;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPageResponse;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationResponseDTO;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationPayload;
import com.umc.connext.domain.mynotification.dto.response.ShareLocationRequestDTO;
import com.umc.connext.domain.mynotification.dto.response.ShareMateRequestDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyNotificationService {
    private final MyNotificationRepository myNotificationRepository;
    private final LocationShareService locationShareService;
    private final MateShareService mateShareService;

    @Transactional(readOnly = true)
    public boolean existsUnread(Long memberId) {

        return myNotificationRepository
                .existsByMemberIdAndIsReadFalse(memberId);
    }

    @Transactional(readOnly = true)
    public MyNotificationPageResponse getMyNotifications(
            Long memberId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

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

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        MyNotification notification = myNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (!notification.getMemberId().equals(memberId)) {
            throw new RuntimeException("권한 없음");
        }

        notification.markAsRead();
    }

    @Transactional
    public void acceptShareLocation(Long memberId, ShareLocationRequestDTO dto){
        locationShareService.accept(memberId, dto.getNotificationId());
    }

    @Transactional
    public void acceptShareMate(Long memberId, ShareMateRequestDTO dto) {
        mateShareService.accept(memberId, dto.getNotificationId());
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
