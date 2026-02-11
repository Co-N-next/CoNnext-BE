package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.entity.MemberVisibilitySetting;
import com.umc.connext.domain.member.enums.PerformanceVisibility;
import com.umc.connext.domain.member.enums.SeatVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "공연 및 좌석 공개 범위 설정 조회 DTO")
@Builder
public class VisibilitySettingResponseDTO {

    @Schema(
            description = "공연 공개 범위",
            example = "TODAY_ONLY"
    )
    private PerformanceVisibility performanceVisibility;

    @Schema(
            description = "좌석 공개 범위",
            example = "EXACT_SEAT"
    )
    private SeatVisibility seatVisibility;

    public static VisibilitySettingResponseDTO from(MemberVisibilitySetting setting) {
        return VisibilitySettingResponseDTO.builder()
                .performanceVisibility(setting.getPerformanceVisibility())
                .seatVisibility(setting.getSeatVisibility())
                .build();
    }
}
