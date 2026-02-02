package com.umc.connext.domain.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Coordinate",
        description = "공연장 내 좌표 정보 (X, Y 및 선택적 층 정보)"
)
public class Coordinate {

    @Schema(
            description = "X 좌표",
            example = "123.45",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal x;

    @Schema(
            description = "Y 좌표",
            example = "678.90",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal y;

    @Schema(
            description = "층 정보 (경로 탐색/표시용, 단일 층일 경우 null 가능)",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer floor;

    public Coordinate(BigDecimal x, BigDecimal y) {
        this.x = x;
        this.y = y;
        this.floor = null;
    }

    public double distanceTo(Coordinate other) {
        double dx = this.x.doubleValue() - other.x.doubleValue();
        double dy = this.y.doubleValue() - other.y.doubleValue();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        if (floor != null) {
            return String.format("(%s층: %.1f, %.1f)", floor, x, y);
        }
        return String.format("(%.1f, %.1f)", x, y);
    }
}
