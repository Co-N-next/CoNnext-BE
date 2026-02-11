package com.umc.connext.domain.mynotification.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import com.umc.connext.domain.mynotification.entity.ActionStatus;
import com.umc.connext.domain.mynotification.entity.ActionType;
import com.umc.connext.domain.mynotification.entity.Category;

@Getter
@Builder
@AllArgsConstructor
public class MyNotificationResponseDTO {

    private Long id;
    private String senderProfileImg;
    private Long senderId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
    private Category category;
    private ActionType actionType;
    private ActionStatus actionStatus;
    private String img;
}
