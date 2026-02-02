package com.umc.connext.domain.venue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PathFindingResponse",
        description = "공연장 내 경로 탐색 결과 응답 DTO (단일 층 / 다층 경로 지원)"
)
public class PathFindingResponse {

    @Schema(
            description = "경로 탐색 성공 여부",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean success;

    @Schema(
            description = "실패 시 에러 메시지",
            example = "경로를 찾을 수 없습니다",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String errorMessage;

    @Schema(
            description = "경로를 구성하는 좌표 목록 (순서대로 연결)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Coordinate> coordinates;

    @Schema(
            description = "전체 경로 거리",
            example = "245.73",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal totalDistance;

    @Schema(
            description = "경로를 구성하는 노드(좌표) 개수",
            example = "25",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int nodeCount;

    @Schema(
            description = "출발 층",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer startFloor;

    @Schema(
            description = "도착 층",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer endFloor;

    @Schema(
            description = "출발 좌표",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Coordinate startPoint;

    @Schema(
            description = "도착 좌표",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Coordinate endPoint;

    @Schema(
            description = "층 전환 정보 목록 (다층 경로일 때만 존재)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<FloorTransition> floorTransitions;

    @Schema(
            description = "다층 경로 여부",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean multiFloor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<NavigationStep> navigationSteps;

    public static PathFindingResponse success(
            List<Coordinate> coordinates,
            BigDecimal totalDistance,
            Integer floor
    ) {
        Coordinate start = coordinates.isEmpty() ? null : coordinates.get(0);
        Coordinate end = coordinates.isEmpty() ? null : coordinates.get(coordinates.size() - 1);

        return PathFindingResponse.builder()
                .success(true)
                .coordinates(coordinates)
                .totalDistance(totalDistance)
                .nodeCount(coordinates.size())
                .startFloor(floor)
                .endFloor(floor)
                .startPoint(start)
                .endPoint(end)
                .floorTransitions(new ArrayList<>())
                .multiFloor(false)
                .build();
    }

    public static PathFindingResponse successMultiFloor(
            List<Coordinate> coordinates,
            BigDecimal totalDistance,
            Integer startFloor,
            Integer endFloor,
            List<FloorTransition> transitions
    ) {
        Coordinate start = coordinates.isEmpty() ? null : coordinates.get(0);
        Coordinate end = coordinates.isEmpty() ? null : coordinates.get(coordinates.size() - 1);

        return PathFindingResponse.builder()
                .success(true)
                .coordinates(coordinates)
                .totalDistance(totalDistance)
                .nodeCount(coordinates.size())
                .startFloor(startFloor)
                .endFloor(endFloor)
                .startPoint(start)
                .endPoint(end)
                .floorTransitions(transitions)
                .multiFloor(true)
                .build();
    }

    public static PathFindingResponse fail(String errorMessage) {
        return PathFindingResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .coordinates(new ArrayList<>())
                .totalDistance(BigDecimal.ZERO)
                .nodeCount(0)
                .floorTransitions(new ArrayList<>())
                .multiFloor(false)
                .build();
    }

    public boolean isEmpty() {
        return coordinates == null || coordinates.isEmpty();
    }
}
