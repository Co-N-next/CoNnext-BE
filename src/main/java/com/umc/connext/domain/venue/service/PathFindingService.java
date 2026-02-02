package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.dto.*;
import com.umc.connext.domain.venue.dto.Coordinate;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueFacilityRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import com.umc.connext.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathFindingService {

    private final VenueSectionRepository sectionRepository;
    private final VenueFacilityRepository facilityRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private static final double GRID_SIZE = 10.0;
    private static final int MAX_ASTAR_ITERATIONS = 20000;

    public PathFindingResponse findPath(Long venueId, PathFindingRequest request) {
        if (request.isSameFloor()) {
            return findPathOnSameFloor(
                    venueId,
                    request.getStartX(), request.getStartY(),
                    request.getEndX(), request.getEndY(),
                    request.getStartFloor()
            );
        }
        return findPathAcrossFloors(venueId, request);
    }

    public PathFindingResponse findPath(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        PathFindingRequest request = PathFindingRequest.builder()
                .startX(startX)
                .startY(startY)
                .startFloor(startFloor)
                .endX(endX)
                .endY(endY)
                .endFloor(endFloor)
                .build();

        return findPath(venueId, request);
    }

    public PathFindingResponse findPathToFacility(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            Long facilityId
    ) {
        VenueFacility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> GeneralException.notFound("해당 시설물을 찾을 수 없습니다. ID=" + facilityId));

        return findPath(
                venueId,
                startX, startY, startFloor,
                facility.getX(), facility.getY(), facility.getFloor()
        );
    }

    private PathFindingResponse findPathOnSameFloor(
            Long venueId,
            BigDecimal startX, BigDecimal startY,
            BigDecimal endX, BigDecimal endY,
            Integer floor
    ) {
        log.info("=== 같은 층 경로 탐색 시작 ===");
        log.info("VenueId: {}, Floor: {}", venueId, floor);
        log.info("출발: ({}, {}), 도착: ({}, {})", startX, startY, endX, endY);

        List<VenueSection> sections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);
        log.info("DB에서 로드된 섹션 개수: {}", sections.size());

        List<Polygon> obstacles = convertToJTSPolygons(sections);
        log.info("장애물 개수: {}", obstacles.size());

        if (obstacles.isEmpty()) {
            log.warn("⚠️ 경고: 장애물이 0개입니다! 모든 경로가 직선으로 처리됩니다.");
        }

        org.locationtech.jts.geom.Coordinate rawStart =
                new org.locationtech.jts.geom.Coordinate(startX.doubleValue(), startY.doubleValue());
        org.locationtech.jts.geom.Coordinate rawEnd =
                new org.locationtech.jts.geom.Coordinate(endX.doubleValue(), endY.doubleValue());

        org.locationtech.jts.geom.Coordinate validStart = findNearestWalkablePoint(rawStart, obstacles);
        org.locationtech.jts.geom.Coordinate validEnd = findNearestWalkablePoint(rawEnd, obstacles);

        if (validStart == null || validEnd == null) {
            log.error("시작점이나 도착점 주변에 이동 가능한 공간이 없습니다.");
            return PathFindingResponse.fail("출발지 또는 도착지 주변에 이동할 수 있는 공간이 없습니다.");
        }

        if (rawStart.distance(validStart) > 0.1) {
            log.info("시작점 보정됨: ({}, {}) -> ({}, {})", rawStart.x, rawStart.y, validStart.x, validStart.y);
        }

        List<org.locationtech.jts.geom.Coordinate> pathCoords = aStarSearch(validStart, validEnd, obstacles);

        if (pathCoords.isEmpty()) {
            log.warn("A* 경로 탐색 실패: {}층, Start({}, {}), End({}, {})",
                    floor, validStart.x, validStart.y, validEnd.x, validEnd.y);
            return PathFindingResponse.fail("경로를 찾을 수 없습니다 (장애물로 막힘)");
        }

        List<Coordinate> coordinates = pathCoords.stream()
                .map(c -> new Coordinate(
                        BigDecimal.valueOf(c.x).setScale(1, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(c.y).setScale(1, RoundingMode.HALF_UP),
                        floor
                ))
                .collect(Collectors.toList());

        BigDecimal totalDistance = calculateTotalDistance(coordinates);

        log.info("=== 같은 층 경로 탐색 완료 ===");
        log.info("최종 경로 좌표 개수: {}, 총 거리: {}", coordinates.size(), totalDistance);

        return PathFindingResponse.success(coordinates, totalDistance, floor);
    }

    private org.locationtech.jts.geom.Coordinate findNearestWalkablePoint(
            org.locationtech.jts.geom.Coordinate target,
            List<Polygon> obstacles
    ) {
        if (!isColliding(target, obstacles)) {
            return target;
        }

        int maxSteps = 10;
        Set<String> visited = new HashSet<>();
        Queue<org.locationtech.jts.geom.Coordinate> queue = new LinkedList<>();

        queue.add(target);
        visited.add(getKey(target));

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        while (!queue.isEmpty()) {
            org.locationtech.jts.geom.Coordinate current = queue.poll();

            if (current.distance(target) > (GRID_SIZE * maxSteps)) continue;

            for (int[] dir : directions) {
                double nx = current.x + (dir[0] * (GRID_SIZE / 2));
                double ny = current.y + (dir[1] * (GRID_SIZE / 2));
                org.locationtech.jts.geom.Coordinate next = new org.locationtech.jts.geom.Coordinate(nx, ny);

                String key = getKey(next);
                if (visited.contains(key)) continue;

                // ✅ 변경: Point 충돌 검사 + LineString 경로 충돌 검사
                if (isColliding(next, obstacles)) {
                    visited.add(key);
                    continue;
                }

                if (isPathColliding(current, next, obstacles)) {
                    visited.add(key);
                    continue;
                }

                return next;
            }
        }

        return null;
    }

    private PathFindingResponse findPathAcrossFloors(Long venueId, PathFindingRequest request) {
        Integer startFloor = request.getStartFloor();
        Integer endFloor = request.getEndFloor();

        List<VenueFacility> stairs = facilityRepository.findStairsConnectingFloors(venueId, startFloor, endFloor);
        if (stairs.isEmpty()) return PathFindingResponse.fail("연결된 계단이 없습니다.");

        VenueFacility bestStairs = findOptimalStairs(stairs, request);
        if (bestStairs == null) return PathFindingResponse.fail("유효한 계단을 찾을 수 없습니다.");

        PathFindingResponse pathToStairs = findPathOnSameFloor(
                venueId,
                request.getStartX(), request.getStartY(),
                bestStairs.getX(), bestStairs.getY(),
                startFloor
        );

        if (!pathToStairs.isSuccess()) return PathFindingResponse.fail("출발지에서 계단까지 경로 없음");

        PathFindingResponse pathFromStairs = findPathOnSameFloor(
                venueId,
                bestStairs.getX(), bestStairs.getY(),
                request.getEndX(), request.getEndY(),
                endFloor
        );

        if (!pathFromStairs.isSuccess()) {
            log.error("계단({})에서 도착점까지 실패.", bestStairs.getName());
            return PathFindingResponse.fail("계단에서 도착점까지 경로를 찾을 수 없습니다");
        }

        return mergePathsWithStairs(pathToStairs, pathFromStairs, bestStairs, startFloor, endFloor);
    }

    private List<org.locationtech.jts.geom.Coordinate> aStarSearch(
            org.locationtech.jts.geom.Coordinate start,
            org.locationtech.jts.geom.Coordinate end,
            List<Polygon> obstacles
    ) {
        if (isColliding(start, obstacles)) {
            log.warn("A* 시작점이 장애물 내부입니다.");
            return Collections.emptyList();
        }

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, start.distance(end));
        openList.add(startNode);
        allNodes.put(getKey(start), startNode);

        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        int iterations = 0;

        while (!openList.isEmpty()) {
            if (iterations++ > MAX_ASTAR_ITERATIONS) {
                log.warn("A* 알고리즘 최대 반복 횟수({}) 초과", MAX_ASTAR_ITERATIONS);
                return Collections.emptyList();
            }

            Node current = openList.poll();
            String currentKey = getKey(current.coord);

            if (closedSet.contains(currentKey)) continue;
            closedSet.add(currentKey);

            if (current.coord.distance(end) < GRID_SIZE) {
                return reconstructPath(current, end);
            }

            for (int[] dir : directions) {
                double newX = current.coord.x + (dir[0] * GRID_SIZE);
                double newY = current.coord.y + (dir[1] * GRID_SIZE);
                org.locationtech.jts.geom.Coordinate neighborCoord = new org.locationtech.jts.geom.Coordinate(newX, newY);

                String neighborKey = getKey(neighborCoord);
                if (closedSet.contains(neighborKey)) continue;

                // 도착점 검사 + 경로 선분 충돌 검사 (LineString 기반)
                if (isColliding(neighborCoord, obstacles)) continue;
                if (isPathColliding(current.coord, neighborCoord, obstacles)) continue;

                // 대각선 이동 시 양쪽 모서리 체크
                if (dir[0] != 0 && dir[1] != 0) {
                    org.locationtech.jts.geom.Coordinate c1 =
                            new org.locationtech.jts.geom.Coordinate(current.coord.x + dir[0] * GRID_SIZE, current.coord.y);
                    org.locationtech.jts.geom.Coordinate c2 =
                            new org.locationtech.jts.geom.Coordinate(current.coord.x, current.coord.y + dir[1] * GRID_SIZE);
                    if (isColliding(c1, obstacles) || isColliding(c2, obstacles)) continue;
                    // 대각선 경로도 검사
                    if (isPathColliding(current.coord, c1, obstacles) ||
                        isPathColliding(current.coord, c2, obstacles)) continue;
                }

                double moveCost = (dir[0] != 0 && dir[1] != 0) ? 1.414 * GRID_SIZE : GRID_SIZE;
                double gCost = current.gCost + moveCost;
                double hCost = neighborCoord.distance(end);

                Node existing = allNodes.get(neighborKey);
                if (existing == null || gCost < existing.gCost) {
                    Node neighbor = new Node(neighborCoord, current, gCost, hCost);
                    allNodes.put(neighborKey, neighbor);
                    openList.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    private String getKey(org.locationtech.jts.geom.Coordinate c) {
        return String.format("%.1f,%.1f", c.x, c.y);
    }

    /**
     * 점(Point)과 선(LineString) 기반 충돌 검사
     * Point 충돌 + 이전 좌표에서 현재 좌표로의 경로(LineString) 충돌을 모두 검사
     */
    private boolean isColliding(org.locationtech.jts.geom.Coordinate coord, List<Polygon> obstacles) {
        Point point = geometryFactory.createPoint(coord);
        for (Polygon polygon : obstacles) {
            if (polygon.intersects(point)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 두 좌표 사이의 경로(LineString)와 장애물의 교차 검사
     * Wall-hack 방지: 점만 검사하는 것이 아니라 선분 전체를 검사
     */
    private boolean isPathColliding(
            org.locationtech.jts.geom.Coordinate from,
            org.locationtech.jts.geom.Coordinate to,
            List<Polygon> obstacles
    ) {
        // 시작점이나 도착점이 장애물 내부인 경우
        if (isColliding(from, obstacles) || isColliding(to, obstacles)) {
            return true;
        }

        // 선분 생성
        LineString path = geometryFactory.createLineString(new org.locationtech.jts.geom.Coordinate[]{from, to});

        // 장애물과의 교차 검사
        for (Polygon obstacle : obstacles) {
            // intersects: 교차하거나 경계를 만질 때 true
            if (path.intersects(obstacle)) {
                // 더 정확한 검사: 선분이 실제로 다각형의 내부를 지나가는지 확인
                if (path.crosses(obstacle) || path.within(obstacle)) {
                    return true;
                }

                // 경계선과의 교차 검사 (매우 얇은 벽도 감지)
                if (path.intersects(obstacle.getBoundary())) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<Polygon> convertToJTSPolygons(List<VenueSection> sections) {
        log.info("=== 장애물 변환 시작 ===");
        log.info("입력된 섹션 개수: {}", sections != null ? sections.size() : 0);

        if (sections == null || sections.isEmpty()) {
            log.warn("섹션 리스트가 NULL 또는 비어있습니다!");
            return new ArrayList<>();
        }

        List<Polygon> polygons = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (int i = 0; i < sections.size(); i++) {
            VenueSection section = sections.get(i);
            log.debug("섹션[{}] 처리 시작 - ID: {}, SectionId: {}", i, section.getId(), section.getSectionId());

            List<Coordinate> vertices = section.getVerticesList();

            if (vertices == null) {
                log.warn("섹션[{}] 의 vertices가 NULL입니다! SectionId: {}", i, section.getSectionId());
                skipCount++;
                continue;
            }

            log.debug("섹션[{}] vertices 개수: {}", i, vertices.size());

            if (vertices.size() < 3) {
                log.warn("섹션[{}] vertices가 3개 미만입니다 (현재: {}) - SectionId: {}", i, vertices.size(), section.getSectionId());
                skipCount++;
                continue;
            }

            try {
                Polygon polygon = createPolygon(vertices);
                polygons.add(polygon);
                successCount++;
                log.debug("섹션[{}] 다각형 생성 성공 - 좌표 수: {}", i, vertices.size());
            } catch (Exception e) {
                failCount++;
                log.error("섹션[{}] 다각형 변환 실패 - Section ID: {}, SectionId: {}", i, section.getId(), section.getSectionId(), e);
            }
        }

        log.info("=== 장애물 변환 완료 ===");
        log.info("총 섹션: {}, 성공: {}, 실패: {}, 스킵: {}",
                sections.size(), successCount, failCount, skipCount);
        log.info("최종 생성된 Polygon 개수: {}", polygons.size());

        return polygons;
    }

    private Polygon createPolygon(List<Coordinate> vertices) {
        try {
            org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];

            log.debug("Polygon 생성 - 정점 수: {}", vertices.size());

            for (int i = 0; i < vertices.size(); i++) {
                double x = vertices.get(i).getX().doubleValue();
                double y = vertices.get(i).getY().doubleValue();
                jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(x, y);
                log.trace("정점[{}]: ({}, {})", i, x, y);
            }

            jtsCoords[vertices.size()] = jtsCoords[0];

            Polygon polygon = geometryFactory.createPolygon(geometryFactory.createLinearRing(jtsCoords));
            log.debug("Polygon 생성 성공 - Area: {}", polygon.getArea());

            return polygon;
        } catch (Exception e) {
            log.error("Polygon 생성 중 예외 발생", e);
            throw e;
        }
    }

    private BigDecimal calculateTotalDistance(List<Coordinate> path) {
        if (path == null || path.size() < 2) return BigDecimal.ZERO;
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) total += path.get(i).distanceTo(path.get(i + 1));
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private VenueFacility findOptimalStairs(List<VenueFacility> stairs, PathFindingRequest request) {
        return stairs.stream().min(Comparator.comparingDouble(s -> {
            double d1 = calculateDistance(
                    request.getStartX().doubleValue(),
                    request.getStartY().doubleValue(),
                    s.getX().doubleValue(),
                    s.getY().doubleValue()
            );
            double d2 = calculateDistance(
                    s.getX().doubleValue(),
                    s.getY().doubleValue(),
                    request.getEndX().doubleValue(),
                    request.getEndY().doubleValue()
            );
            return d1 + d2;
        })).orElse(null);
    }

    private PathFindingResponse mergePathsWithStairs(
            PathFindingResponse p1,
            PathFindingResponse p2,
            VenueFacility stairs,
            int f1,
            int f2
    ) {
        List<Coordinate> merged = new ArrayList<>(p1.getCoordinates());
        int transIdx = merged.size() - 1;

        List<Coordinate> p2Coords = p2.getCoordinates();
        if (!p2Coords.isEmpty()) {
            for (int i = 1; i < p2Coords.size(); i++) merged.add(p2Coords.get(i));
        }

        BigDecimal totalDist = p1.getTotalDistance().add(p2.getTotalDistance());
        FloorTransition trans = FloorTransition.from(stairs, f1, f2, transIdx);

        return PathFindingResponse.successMultiFloor(merged, totalDist, f1, f2, List.of(trans));
    }

    private List<org.locationtech.jts.geom.Coordinate> reconstructPath(
            Node endNode,
            org.locationtech.jts.geom.Coordinate realEnd
    ) {
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

    private static class Node {
        final org.locationtech.jts.geom.Coordinate coord;
        Node parent;
        double gCost, hCost, fCost;

        Node(org.locationtech.jts.geom.Coordinate c, Node p, double g, double h) {
            this.coord = c;
            this.parent = p;
            this.gCost = g;
            this.hCost = h;
            this.fCost = g + h;
        }
    }
}
