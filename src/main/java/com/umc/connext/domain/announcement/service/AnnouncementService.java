package com.umc.connext.domain.announcement.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.umc.connext.domain.announcement.repository.AnnouncementRepository;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;

    public boolean existsUnread() {
        return repository.existsByIsReadFalse();
    }
}
