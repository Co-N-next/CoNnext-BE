package com.umc.connext.domain.announcement.service;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.umc.connext.domain.announcement.dto.response.*;
import com.umc.connext.domain.announcement.entity.Announcement;
import com.umc.connext.domain.announcement.entity.AnnouncementReadStatus;
import com.umc.connext.domain.announcement.repository.AnnouncementReadStatusRepository;
import com.umc.connext.domain.announcement.repository.AnnouncementRepository;
import com.umc.connext.common.response.PageInfo;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository repository;
    private final AnnouncementReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    public boolean existsUnread(Long memberId) {
        return readStatusRepository.existsByMember_IdAndIsReadFalse(memberId);
    }

    // 공지사항 목록 조회
    public AnnouncementPageResponse getAnnouncements(
            Long memberId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
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

    // 공지 생성
    @Transactional
    public void createAnnouncement(
            Long adminId,
            AnnouncementCreateRequestDTO request
    ) {

        Announcement announcement = Announcement.builder()
                .senderId(adminId)
                .title(request.getTitle())
                .content(request.getContent())
                .logoImg(request.getLogoImg())
                .build();

        repository.save(announcement);

        // 모든 활성 유저에 대해 읽음 상태 생성
        List<Long> memberIds = memberRepository.findAllActiveMemberIds();

        List<AnnouncementReadStatus> readStatuses = memberIds.stream()
                .map(memberId -> {
                    Member member = memberRepository.getReferenceById(memberId);
                    return AnnouncementReadStatus.create(announcement, member);
                })
                .toList();

        readStatusRepository.saveAll(readStatuses);
    }

    // 읽음 처리
    @Transactional
    public void markAsRead(Long announcementId, Long memberId) {

        Announcement announcement = repository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        AnnouncementReadStatus status = readStatusRepository
                .findByAnnouncementAndMember(announcement, member)
                .orElseThrow(() -> new IllegalStateException("읽음 상태가 존재하지 않습니다."));

        status.markAsRead();
        readStatusRepository.save(status);
    }

    public List<Announcement> getUnreadAnnouncements(Long memberId) {

        return readStatusRepository
                .findAllByMember_IdAndIsReadFalse(memberId)
                .stream()
                .map(AnnouncementReadStatus::getAnnouncement)
                .toList();
    }

    // 특정 공지 읽은 수
    public Long getReadCount(Long announcementId) {

        Announcement announcement = repository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));

        return readStatusRepository.countByAnnouncementAndIsReadTrue(announcement);
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
