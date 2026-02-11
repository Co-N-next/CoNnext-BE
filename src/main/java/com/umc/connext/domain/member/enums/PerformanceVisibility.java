package com.umc.connext.domain.member.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "공연 공개 범위 (TODAY_ONLY: 오늘 공연만, ALL_RESERVED: 예매한 모든 공연)"
)
public enum PerformanceVisibility {
    TODAY_ONLY,
    ALL_RESERVED
}
