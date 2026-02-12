package com.umc.connext.domain.announcement.repository;

import com.umc.connext.domain.announcement.entity.Announcement;
import com.umc.connext.domain.announcement.entity.AnnouncementReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.umc.connext.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface AnnouncementReadStatusRepository
        extends JpaRepository<AnnouncementReadStatus, Long> {

    Optional<AnnouncementReadStatus> findByAnnouncementAndMember(
            Announcement announcement,
            Member member
    );

    boolean existsByMember_IdAndIsReadFalse(Long memberId);

    List<AnnouncementReadStatus> findAllByMember_IdAndIsReadFalse(Long memberId);

    Long countByAnnouncementAndIsReadTrue(Announcement announcement);
}
