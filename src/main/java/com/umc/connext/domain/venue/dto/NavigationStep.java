package com.umc.connext.domain.venue.dto;

import com.umc.connext.common.enums.DirectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationStep {
    private int stepOrder;          // 순서 (1, 2, 3...)
    private String description;     // 안내 문구 (예: "30m 직진 후 우회전하세요")
    private DirectionType type;     // 아이콘 표시용 (STRAIGHT, LEFT, RIGHT, STAIRS_UP...)
    private BigDecimal distance;    // 해당 구간 이동 거리
    private Coordinate coordinate;  // 해당 안내가 발생하는 지점 (x, y, floor)
}