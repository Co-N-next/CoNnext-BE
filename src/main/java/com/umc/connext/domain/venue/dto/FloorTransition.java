package com.umc.connext.domain.venue.dto;

import com.umc.connext.domain.venue.entity.VenueFacility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "FloorTransition",
        description = "경로 탐색 중 층 전환 정보 (계단/엘리베이터 등)"
)
public class FloorTransition {

    @Schema(
            description = "계단(또는 층 전환 시설) ID",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long stairsId;

    @Schema(
            description = "계단(또는 층 전환 시설) 이름",
            example = "중앙 계단",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String stairsName;

    @Schema(
            description = "출발 층",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer fromFloor;

    @Schema(
            description = "도착 층",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer toFloor;

    @Schema(
            description = "층 전환 지점 X 좌표",
            example = "345.67",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal x;

    @Schema(
            description = "층 전환 지점 Y 좌표",
            example = "890.12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal y;

    @Schema(
            description = "전체 경로 내 인덱스 (path 좌표 리스트 기준)",
            example = "12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int pathIndex;

    public static FloorTransition from(
            VenueFacility stairs,
            Integer fromFloor,
            Integer toFloor,
            int pathIndex
    ) {
        return FloorTransition.builder()
                .stairsId(stairs.getId())
                .stairsName(stairs.getName() != null ? stairs.getName() : "계단")
                .fromFloor(fromFloor)
                .toFloor(toFloor)
                .x(stairs.getX())
                .y(stairs.getY())
                .pathIndex(pathIndex)
                .build();
    }
}
