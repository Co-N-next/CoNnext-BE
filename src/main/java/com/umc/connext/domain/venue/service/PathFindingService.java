package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.dto.Coordinate;
import com.umc.connext.domain.venue.dto.PathFindingResponse;
import com.umc.connext.domain.venue.entity.VenueNode;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueNodeRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon; // JTS Polygon
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathFindingService {

    private final VenueSectionRepository sectionRepository;
    private final VenueNodeRepository nodeRepository; // 시설물 조회용으로 유지

    private final GeometryFactory geometryFactory = new GeometryFactory();

    // 그리드 정밀도 (이 값이 작을수록 경로가 부드럽지만 계산이 오래 걸림)
    // 좌표 단위가 1000단위이므로 20~30 정도가 적당
    private static final double GRID_SIZE = 20.0;

    /**
     * 장애물을 회피하여 두 좌표 사이의 최단 경로 찾기 (A* Algorithm)
     */
    public PathFindingResponse findPath(Long venueId,
                                        BigDecimal startX, BigDecimal startY,
                                        BigDecimal endX, BigDecimal endY) {

        // 1. 해당 공연장의 모든 구역(장애물) 조회 및 JTS Polygon 변환
        List<VenueSection> sections = sectionRepository.findAllByVenueId(venueId);
        List<Polygon> obstacles = convertToJTSPolygons(sections);

        // 2. 시작/종료 좌표 변환
        org.locationtech.jts.geom.Coordinate startCoord =
                new org.locationtech.jts.geom.Coordinate(startX.doubleValue(), startY.doubleValue());
        org.locationtech.jts.geom.Coordinate endCoord =
                new org.locationtech.jts.geom.Coordinate(endX.doubleValue(), endY.doubleValue());

        // 3. A* 알고리즘 실행
        List<org.locationtech.jts.geom.Coordinate> pathCoords = aStarSearch(startCoord, endCoord, obstacles);

        // 4. 결과 매핑 (JTS Coordinate -> VenueNode/DTO)
        if (pathCoords.isEmpty()) {
            return PathFindingResponse.builder()
                    .path(Collections.emptyList())
                    .totalDistance(BigDecimal.ZERO)
                    .nodeCount(0)
                    .build();
        }

        List<VenueNode> pathNodes = convertToVenueNodes(venueId, pathCoords);
        BigDecimal totalDistance = calculateTotalDistance(pathNodes);

        return PathFindingResponse.builder()
                .path(pathNodes)
                .totalDistance(totalDistance)
                .nodeCount(pathNodes.size())
                .startNodeId(-1L) // 가상 ID
                .endNodeId(-2L)   // 가상 ID
                .coordinates(pathCoords.stream()
                        .map(c -> new Coordinate(BigDecimal.valueOf(c.x), BigDecimal.valueOf(c.y)))
                        .collect(Collectors.toList()))
                .build();
    }

    // =========================================================================
    // A* 알고리즘 핵심 로직
    // =========================================================================

    private List<org.locationtech.jts.geom.Coordinate> aStarSearch(
            org.locationtech.jts.geom.Coordinate start,
            org.locationtech.jts.geom.Coordinate end,
            List<Polygon> obstacles) {

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, start.distance(end));
        openList.add(startNode);
        allNodes.put(getKey(start), startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            // 목표 지점 도달 체크 (Grid 오차 범위 고려)
            if (current.coord.distance(end) < GRID_SIZE) {
                return reconstructPath(current, end);
            }

            // 8방향 탐색 (상하좌우 + 대각선)
            int[][] directions = {
                    {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            };

            for (int[] dir : directions) {
                double newX = current.coord.x + (dir[0] * GRID_SIZE);
                double newY = current.coord.y + (dir[1] * GRID_SIZE);
                org.locationtech.jts.geom.Coordinate neighborCoord =
                        new org.locationtech.jts.geom.Coordinate(newX, newY);

                String key = getKey(neighborCoord);

                // 장애물 충돌 체크
                if (isColliding(neighborCoord, obstacles)) {
                    continue;
                }

                double moveCost = (dir[0] != 0 && dir[1] != 0) ? 1.414 * GRID_SIZE : GRID_SIZE;
                double gCost = current.gCost + moveCost;
                double hCost = neighborCoord.distance(end);

                Node neighbor = allNodes.getOrDefault(key, new Node(neighborCoord, null, Double.MAX_VALUE, 0));

                if (gCost < neighbor.gCost) {
                    neighbor.parent = current;
                    neighbor.gCost = gCost;
                    neighbor.hCost = hCost;
                    neighbor.fCost = gCost + hCost;

                    if (!allNodes.containsKey(key)) {
                        allNodes.put(key, neighbor);
                        openList.add(neighbor);
                    } else {
                        // PriorityQueue 갱신을 위해 제거 후 재삽입
                        openList.remove(neighbor);
                        openList.add(neighbor);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean isColliding(org.locationtech.jts.geom.Coordinate coord, List<Polygon> obstacles) {
        Point point = geometryFactory.createPoint(coord);
        for (Polygon polygon : obstacles) {
            if (polygon.intersects(point)) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // 헬퍼 메소드 (변환 및 유틸리티)
    // =========================================================================

    private List<Polygon> convertToJTSPolygons(List<VenueSection> sections) {
        List<Polygon> polygons = new ArrayList<>();
        for (VenueSection section : sections) {
            List<Coordinate> vertices = section.getVerticesList(); // 기존 DTO의 Coordinate 사용
            if (vertices == null || vertices.size() < 3) continue;

            org.locationtech.jts.geom.Coordinate[] coordinates = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];
            for (int i = 0; i < vertices.size(); i++) {
                coordinates[i] = new org.locationtech.jts.geom.Coordinate(
                        vertices.get(i).getX().doubleValue(),
                        vertices.get(i).getY().doubleValue()
                );
            }
            // 다각형 닫기 (시작점 = 끝점)
            coordinates[vertices.size()] = coordinates[0];

            try {
                LinearRing shell = geometryFactory.createLinearRing(coordinates);
                polygons.add(geometryFactory.createPolygon(shell));
            } catch (Exception e) {
                // 유효하지 않은 다각형 스킵
            }
        }
        return polygons;
    }

    private List<VenueNode> convertToVenueNodes(Long venueId, List<org.locationtech.jts.geom.Coordinate> coords) {
        List<VenueNode> nodes = new ArrayList<>();
        long tempId = -1L;
        for (org.locationtech.jts.geom.Coordinate c : coords) {
            nodes.add(VenueNode.builder()
                    .nodeId(tempId--)
                    .venueId(venueId)
                    .x(BigDecimal.valueOf(c.x))
                    .y(BigDecimal.valueOf(c.y))
                    .build());
        }
        return nodes;
    }

    private List<org.locationtech.jts.geom.Coordinate> reconstructPath(Node endNode, org.locationtech.jts.geom.Coordinate realEnd) {
        List<org.locationtech.jts.geom.Coordinate> path = new ArrayList<>();
        path.add(realEnd);
        Node current = endNode;
        while (current != null) {
            path.add(current.coord);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private String getKey(org.locationtech.jts.geom.Coordinate c) {
        // 부동 소수점 오차 방지를 위해 정수형 키 사용
        return Math.round(c.x) + "," + Math.round(c.y);
    }

    private BigDecimal calculateTotalDistance(List<VenueNode> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            double dx = path.get(i).getX().doubleValue() - path.get(i+1).getX().doubleValue();
            double dy = path.get(i).getY().doubleValue() - path.get(i+1).getY().doubleValue();
            total += Math.sqrt(dx*dx + dy*dy);
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 특정 좌표가 구역(장애물) 내에 있는지 확인 (JTS 활용)
     */
    public boolean isPointInSection(Long venueId, String sectionId, BigDecimal x, BigDecimal y) {
        // 1. 해당 구역(Section) 정보 조회
        VenueSection section = sectionRepository.findByVenueIdAndSectionId(venueId, sectionId);
        if (section == null) return false;

        // 2. 구역의 좌표들을 JTS Polygon으로 변환
        List<Coordinate> vertices = section.getVerticesList(); // DTO Coordinate
        if (vertices == null || vertices.size() < 3) return false;

        org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++) {
            jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(
                    vertices.get(i).getX().doubleValue(),
                    vertices.get(i).getY().doubleValue()
            );
        }
        jtsCoords[vertices.size()] = jtsCoords[0]; // 다각형 닫기

        try {
            Polygon polygon = geometryFactory.createPolygon(jtsCoords);
            Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(x.doubleValue(), y.doubleValue()));

            // 3. 포함 여부 확인 (contains: 경계 제외 내부, intersects: 경계 포함)
            return polygon.intersects(point);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 특정 구역 내의 시설물까지의 경로 찾기 (시설물 ID 이용)
     */
    public PathFindingResponse findPathToFacility(Long venueId,
                                                  BigDecimal startX, BigDecimal startY,
                                                  Long facilityId) {
        VenueNode facilityNode = nodeRepository.findById(facilityId).orElse(null);
        if (facilityNode != null) {
            return findPath(venueId, startX, startY, facilityNode.getX(), facilityNode.getY());
        }
        return PathFindingResponse.builder().path(Collections.emptyList()).build();
    }

    // A* Node 내부 클래스
    private static class Node {
        org.locationtech.jts.geom.Coordinate coord;
        Node parent;
        double gCost;
        double hCost;
        double fCost;

        public Node(org.locationtech.jts.geom.Coordinate coord, Node parent, double gCost, double hCost) {
            this.coord = coord;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}