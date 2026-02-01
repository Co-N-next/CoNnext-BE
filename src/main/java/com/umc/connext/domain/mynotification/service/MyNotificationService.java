package com.umc.connext.domain.mynotification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.umc.connext.domain.mynotification.repository.MyNotificationRepository;

@Service
@RequiredArgsConstructor
public class MyNotificationService {
    private final MyNotificationRepository myNotificationRepository;

    public boolean existsUnread() {
        return myNotificationRepository.existsByIsReadFalse();
    }
}
