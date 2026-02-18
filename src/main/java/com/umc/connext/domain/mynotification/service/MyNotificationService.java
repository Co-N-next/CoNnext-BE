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
import com.umc.connext.domain.mate.service.MateService;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.mate.entity.Mate;
import com.umc.connext.domain.mate.repository.MateRepository;
import com.umc.connext.domain.mate.enums.MateStatus;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.code.ErrorCode;


@Service
@RequiredArgsConstructor
public class MyNotificationService {
    private final MyNotificationRepository myNotificationRepository;
    private final LocationShareService locationShareService;
    private final MateShareService mateShareService;
    private final MateService mateService;
    private final MateRepository mateRepository;

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
    public String markAsRead(Long memberId, Long notificationId) {
        var notificationOpt = myNotificationRepository.findById(notificationId);

        if (notificationOpt.isEmpty()) {
            // 알림이 없으면 200과 메시지 반환
            return "알림 없음";
        }

        MyNotification notification = notificationOpt.get();

        if (!notification.getMemberId().equals(memberId)) {
            throw new RuntimeException("권한 없음");
        }

        notification.markAsRead();
        return "읽음 처리 완료";
    }


    @Transactional
    public String acceptShareLocation(Long memberId, ShareLocationRequestDTO dto) {
        var notificationOpt = myNotificationRepository.findById(dto.getNotificationId());

        if (notificationOpt.isEmpty()) {
            return "알림 없음";
        }

        MyNotification notification = notificationOpt.get();

        if (!notification.getMemberId().equals(memberId)) {
            return "권한 없음";
        }

        if (notification.getActionStatus() != ActionStatus.PENDING) {
            return "이미 처리된 알림";
        }

        try {
            boolean result = locationShareService.accept(memberId, dto.getNotificationId());
            if (result) {
                notification.accept(); // 알림 상태 변경
                return "위치 공유 수락 완료";
            } else {
                return "처리 실패";
            }
        } catch (Exception e) {
            // 서비스 내부에서 발생한 예외도 잡아서 메시지 반환
            return "처리 실패: " + e.getMessage();
        }
    }


    @Transactional
    public String acceptShareMate(Long memberId, ShareMateRequestDTO dto) {
        var notificationOpt = myNotificationRepository.findById(dto.getNotificationId());

        if (notificationOpt.isEmpty()) {
            return "알림 없음";
        }

        MyNotification notification = notificationOpt.get();

        // 수신자 확인
        if (!notification.getMemberId().equals(memberId)) {
            return "권한 없음";
        }

        // 이미 처리된 알림 확인
        if (notification.getActionStatus() != ActionStatus.PENDING) {
            return "이미 처리된 알림";
        }

        // 알림 카테고리 확인
        if (notification.getCategory() != Category.MATE) {
            return "메이트 요청 알림이 아님";
        }

        try {
            // Mate 엔티티 조회 (senderId=요청자, memberId=수신자)
            Mate mate = mateRepository.findBetween(notification.getSenderId(), memberId)
                    .orElseThrow(() -> new GeneralException(ErrorCode.MATE_NOT_FOUND, "해당 메이트 요청이 존재하지 않습니다."));

            // 상태 수락
            mateService.acceptMateRequest(memberId, mate.getId());

            // 알림 상태 변경
            notification.accept();

            return "메이트 요청 수락 완료";
        } catch (GeneralException e) {
            return "처리 실패: " + e.getMessage();
        } catch (Exception e) {
            return "처리 실패: 알 수 없는 오류 발생";
        }
    }


    @Transactional
    public void createMateRequestNotification(
            Member sender,
            Member receiver,
            Long mateId
    ){
        MyNotification notification = MyNotification.builder()
                .memberId(receiver.getId())
                .senderId(sender.getId())
                .title("메이트 요청")
                .content(sender.getNickname() + "님이 메이트 요청을 보냈습니다")
                .category(Category.MATE)
                .actionType(ActionType.ACCEPT_REJECT)
                .actionStatus(ActionStatus.PENDING)
                .img(sender.getProfileImage())
                .build();

        myNotificationRepository.save(notification);
    }


    @Transactional
    public boolean accept(Long memberId, Long notificationId) {
        var notificationOpt = myNotificationRepository.findByMyNotificationIdAndMemberId(notificationId, memberId);

        if (notificationOpt.isEmpty()) {
            return false;
        }

        MyNotification notification = notificationOpt.get();

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
        return true;
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
