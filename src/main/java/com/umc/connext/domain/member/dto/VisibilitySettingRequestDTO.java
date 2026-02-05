package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.enums.PerformanceVisibility;
import com.umc.connext.domain.member.enums.SeatVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "공연 및 좌석 공개 범위 변경 요청 DTO")
public class VisibilitySettingRequestDTO {

    @NotNull
    @Schema(
            description = "공연 공개 범위",
            example = "TODAY_ONLY",
            allowableValues = {
                    "TODAY_ONLY",
                    "ALL_MY_BOOKED_PERFORMANCES"
            }
    )
    private PerformanceVisibility performanceVisibility;

    @NotNull
    @Schema(
            description = "좌석 공개 레벨",
            example = "SECTION_ROW",
            allowableValues = {
                    "SECTION_ONLY",
                    "ROW_ONLY",
                    "EXACT_SEAT"
            }
    )
    private SeatVisibility seatVisibility;
}
