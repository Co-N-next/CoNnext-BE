package com.umc.connext.domain.announcement.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.umc.connext.domain.announcement.dto.response.AnnouncementCreateRequestDTO;
import com.umc.connext.domain.announcement.repository.AnnouncementRepository;
import com.umc.connext.domain.announcement.repository.AnnouncementReadStatusRepository;
import com.umc.connext.domain.announcement.entity.Announcement;
import com.umc.connext.domain.announcement.entity.AnnouncementReadStatus;
import com.umc.connext.domain.announcement.dto.response.AnnouncementPageResponse;
import com.umc.connext.domain.announcement.dto.response.AnnouncementResponseDTO;
import com.umc.connext.domain.announcement.dto.response.AnnouncementPayload;
import com.umc.connext.common.response.PageInfo;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {
    private final AnnouncementRepository repository;
    private final AnnouncementReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;
    private final MyNotificationRepository myNotificationRepository;

    @Transactional(readOnly = true)
    public boolean existsUnread(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<Announcement> unreadAnnouncements =
                readStatusRepository.findUnreadAnnouncementsByMember(member);

        return !unreadAnnouncements.isEmpty();
    }

    public AnnouncementPageResponse getAnnouncements(
            Long memberId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Announcement> result = repository.findAll(pageable);

        List<AnnouncementResponseDTO> notices = result.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        PageInfo pageInfo = PageInfo.of(result);

        return new AnnouncementPageResponse(
                pageInfo,
                new AnnouncementPayload(notices)
        );
    }

    @Transactional
    public void createAnnouncement(
            Long adminId,
            AnnouncementCreateRequestDTO request
    ) {
        // 1. 공지사항 생성
        Announcement announcement = Announcement.builder()
                .senderId(adminId)
                .title(request.getTitle())
                .content(request.getContent())
                .logoImg(request.getLogoImg())
                .build();

        repository.save(announcement);

        // 2. 모든 활성 유저에 대해 읽음 상태 레코드 생성
        List<Long> memberIds = memberRepository.findAllActiveMemberIds();

        List<AnnouncementReadStatus> readStatuses = memberIds.stream()
                .map(memberId -> {
                    Member member = memberRepository.getReferenceById(memberId);
                    return AnnouncementReadStatus.create(announcement, member);
                })
                .toList();

        readStatusRepository.saveAll(readStatuses);
    }

    @Transactional
    public void markAsRead(Long announcementId, Long memberId) {
        Announcement announcement = repository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        AnnouncementReadStatus status = readStatusRepository
                .findByAnnouncementAndMember(announcement, member)
                .orElseGet(() -> AnnouncementReadStatus.create(announcement, member));

        status.markAsRead();
        readStatusRepository.save(status);
    }

    public List<Announcement> getUnreadAnnouncements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return readStatusRepository.findUnreadAnnouncementsByMember(member);
    }

    public Long getReadCount(Long announcementId) {
        Announcement announcement = repository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        return readStatusRepository.countReadByAnnouncement(announcement);
    }

    private AnnouncementResponseDTO toDto(Announcement entity) {
        return AnnouncementResponseDTO.builder()
                .id(entity.getId())
                .logoImg(entity.getLogoImg())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
