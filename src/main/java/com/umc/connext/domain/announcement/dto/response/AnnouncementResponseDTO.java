package com.umc.connext.domain.announcement.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnnouncementResponseDTO {
    private Long id;
    private String logoImg;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
}
