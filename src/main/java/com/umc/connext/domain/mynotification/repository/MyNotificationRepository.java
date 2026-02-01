package com.umc.connext.domain.mynotification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.umc.connext.domain.mynotification.entity.MyNotification;

public interface MyNotificationRepository extends JpaRepository<MyNotification, Long>{
    boolean existsByIsReadFalse();
}
