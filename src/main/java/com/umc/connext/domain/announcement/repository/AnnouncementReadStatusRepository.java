package com.umc.connext.domain.announcement.repository;

import com.umc.connext.domain.announcement.entity.Announcement;
import com.umc.connext.domain.announcement.entity.AnnouncementReadStatus;
import com.umc.connext.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AnnouncementReadStatusRepository extends JpaRepository<AnnouncementReadStatus, Long> {

    Optional<AnnouncementReadStatus> findByAnnouncementAndMember(
            Announcement announcement,
            Member member
    );

    // 특정 유저가 읽지 않은 공지 조회
    @Query("SELECT a FROM Announcement a " +
            "WHERE NOT EXISTS (SELECT 1 FROM AnnouncementReadStatus ars " +
            "WHERE ars.announcement = a AND ars.member = :member AND ars.isRead = true)")
    List<Announcement> findUnreadAnnouncementsByMember(@Param("member") Member member);

    // 특정 공지의 읽음 수 조회
    @Query("SELECT COUNT(ars) FROM AnnouncementReadStatus ars " +
            "WHERE ars.announcement = :announcement AND ars.isRead = true")
    Long countReadByAnnouncement(@Param("announcement") Announcement announcement);
}
