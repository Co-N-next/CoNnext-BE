package com.umc.connext.domain.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PathFindingRequest",
        description = "공연장 내 경로 탐색 요청 DTO (출발/도착 좌표 및 층 정보)"
)
public class PathFindingRequest {

    @Schema(
            description = "출발 X 좌표",
            example = "123.45",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "시작 X 좌표는 필수입니다")
    private BigDecimal startX;

    @Schema(
            description = "출발 Y 좌표",
            example = "678.90",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "시작 Y 좌표는 필수입니다")
    private BigDecimal startY;

    @Schema(
            description = "출발 층",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "시작 층은 필수입니다")
    private Integer startFloor;

    @Schema(
            description = "도착 X 좌표",
            example = "223.45",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "도착 X 좌표는 필수입니다")
    private BigDecimal endX;

    @Schema(
            description = "도착 Y 좌표",
            example = "778.90",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "도착 Y 좌표는 필수입니다")
    private BigDecimal endY;

    @Schema(
            description = "도착 층",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "도착 층은 필수입니다")
    private Integer endFloor;

    public boolean isSameFloor() {
        return startFloor.equals(endFloor);
    }

    public Coordinate getStartCoordinate() {
        return new Coordinate(startX, startY, startFloor);
    }

    public Coordinate getEndCoordinate() {
        return new Coordinate(endX, endY, endFloor);
    }
}
