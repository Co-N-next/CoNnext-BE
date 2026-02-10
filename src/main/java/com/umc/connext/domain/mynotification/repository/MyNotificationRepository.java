package com.umc.connext.domain.mynotification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.umc.connext.domain.mynotification.entity.MyNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.Optional;

public interface MyNotificationRepository extends JpaRepository<MyNotification, Long>{
    boolean existsByMemberIdAndIsReadFalse(Long memberId);

    Page<MyNotification> findAllByMemberId(
            Long memberId,
            Pageable pageable
    );

    Optional<MyNotification> findByMyNotificationIdAndMemberId(
            Long notificationId,
            Long memberId
    );


    Long countByMemberIdAndIsReadFalse(Long memberId);
}
