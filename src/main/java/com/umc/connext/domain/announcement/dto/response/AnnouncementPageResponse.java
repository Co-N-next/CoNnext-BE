package com.umc.connext.domain.announcement.dto.response;

import com.umc.connext.domain.announcement.dto.response.AnnouncementPayload;
import lombok.Getter;
import lombok.AllArgsConstructor;
import com.umc.connext.common.response.PageInfo;

@Getter
@AllArgsConstructor
public class AnnouncementPageResponse {

    private PageInfo pageInfo;
    private AnnouncementPayload payload;

}
