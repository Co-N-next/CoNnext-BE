package com.umc.connext.domain.announcement.service;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.umc.connext.domain.announcement.dto.response.AnnouncementCreateRequestDTO;
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

    public AnnouncementPageResponse getAnnouncements(
            Long memberId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Announcement> result = repository.findAll( pageable);

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
