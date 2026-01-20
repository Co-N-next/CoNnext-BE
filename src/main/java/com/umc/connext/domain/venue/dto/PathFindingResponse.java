package com.umc.connext.domain.venue.dto;

import com.umc.connext.domain.venue.entity.VenueNode;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PathFindingResponse {
    private List<VenueNode> path;
    private BigDecimal totalDistance;
    private int nodeCount;
    private Long startNodeId;
    private Long endNodeId;
    private List<Coordinate> coordinates;

    public static PathFindingResponse from(List<VenueNode> path, BigDecimal totalDistance) {
        List<Coordinate> coords = path.stream()
                .map(node -> new Coordinate(node.getX(), node.getY()))
                .toList();

        return PathFindingResponse.builder()
                .path(path)
                .totalDistance(totalDistance)
                .nodeCount(path.size())
                .startNodeId(path.isEmpty() ? null : path.get(0).getNodeId())
                .endNodeId(path.isEmpty() ? null : path.get(path.size() - 1).getNodeId())
                .coordinates(coords)
                .build();
    }
}