package com.umc.connext.domain.mynotification.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.umc.connext.domain.mynotification.dto.response.MyNotificationResponseDTO;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyNotificationPayload {
    private List<MyNotificationResponseDTO> news;
}

