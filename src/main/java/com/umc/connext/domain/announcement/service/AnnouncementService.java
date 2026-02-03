package com.umc.connext.domain.announcement.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.umc.connext.domain.announcement.repository.AnnouncementRepository;
import com.umc.connext.domain.announcement.entity.Announcement;
import com.umc.connext.domain.announcement.dto.response.AnnouncementPageResponse;
import com.umc.connext.domain.announcement.dto.response.AnnouncementResponseDTO;
import com.umc.connext.domain.announcement.dto.response.AnnouncementPayload;
import com.umc.connext.common.response.PageInfo;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;

    public boolean existsUnread() {
        return repository.existsByIsReadFalse();
    }

    public AnnouncementPageResponse getAnnouncements(int page, int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 수정 필요
        Long memberId = 1L;

        // senderId? 수정 필요
        Page<Announcement> result = repository.findAll(pageable);

        List<AnnouncementResponseDTO> notices = result.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        PageInfo pageInfo = PageInfo.of(result);

        return new
                AnnouncementPageResponse(
                pageInfo,
                new AnnouncementPayload(notices)
        );
    }

    private AnnouncementResponseDTO toDto(Announcement entity) {
        return AnnouncementResponseDTO.builder()
                .id(entity.getId())
                .logoImg(entity.getLogoImg())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.getIsRead())
                .build();
    }
}
