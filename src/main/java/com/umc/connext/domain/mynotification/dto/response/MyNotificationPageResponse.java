package com.umc.connext.domain.mynotification.dto.response;

import com.umc.connext.domain.mynotification.dto.response.MyNotificationPayload;
import lombok.Getter;
import lombok.AllArgsConstructor;
import com.umc.connext.common.response.PageInfo;

@Getter
@AllArgsConstructor
public class MyNotificationPageResponse {

    private PageInfo pageInfo;
    private MyNotificationPayload payload;
}

