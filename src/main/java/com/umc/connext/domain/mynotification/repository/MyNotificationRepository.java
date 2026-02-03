package com.umc.connext.domain.mynotification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface MyNotificationRepository extends JpaRepository<MyNotification, Long>{
    boolean existsByIsReadFalse();

    Page<MyNotification> findAllByMemberId(
            Long memberId,
            Pageable pageable
    );
}
