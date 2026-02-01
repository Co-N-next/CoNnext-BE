package com.umc.connext.domain.announcement.repository;

import com.umc.connext.domain.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    boolean existsByIsReadFalse();
}
