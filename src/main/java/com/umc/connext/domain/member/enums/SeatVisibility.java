package com.umc.connext.domain.member.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "좌석 공개 범위 단계 " +
                "(SECTION_ONLY: 구역만 공개, " +
                "ROW_ONLY: 구역 + 열 공개, " +
                "EXACT_SEAT: 정확한 좌석까지 공개)"
)
public enum SeatVisibility {
    SECTION_ONLY,
    ROW_ONLY,
    EXACT_SEAT
}
