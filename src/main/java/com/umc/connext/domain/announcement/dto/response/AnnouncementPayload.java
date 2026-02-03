package com.umc.connext.domain.announcement.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.umc.connext.domain.announcement.dto.response.AnnouncementResponseDTO;
import java.util.List;

@Getter
@AllArgsConstructor
public class AnnouncementPayload {
    private List<AnnouncementResponseDTO> notices;
}
