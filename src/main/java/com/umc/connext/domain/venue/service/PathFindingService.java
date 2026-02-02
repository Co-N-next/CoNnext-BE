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
    // 층 탐색 범위 (지하 2층 ~ 지상 10층, 필요에 따라 조정)
    private static final int MIN_SEARCH_FLOOR = -2;
    private static final int MAX_SEARCH_FLOOR = 10;

    /**
     * 메인 경로 탐색 메서드
     */
    public PathFindingResponse findPath(Long venueId, PathFindingRequest request) {
        log.info("=== 경로 탐색 요청 수신 ===");
        log.info("요청 원본: Start({}층, {}, {}), End({}층, {}, {})",
                request.getStartFloor(), request.getStartX(), request.getStartY(),
                request.getEndFloor(), request.getEndX(), request.getEndY());

        // [핵심 수정] 1. 층 정보 자동 보정 (좌표 기반)
        int fixedStartFloor = validateAndCorrectFloor(venueId, request.getStartX(), request.getStartY(), request.getStartFloor());
        int fixedEndFloor = validateAndCorrectFloor(venueId, request.getEndX(), request.getEndY(), request.getEndFloor());

        // 보정된 정보로 요청 객체 업데이트 (빌더 패턴 활용 권장, 여기선 setter가 없다고 가정하고 로컬 변수 사용)
        // 실제 로직 분기 시 fixedStartFloor, fixedEndFloor를 사용합니다.

        if (fixedStartFloor != request.getStartFloor() || fixedEndFloor != request.getEndFloor()) {
            log.warn("🔄 층 정보 자동 보정됨: Start({} -> {}), End({} -> {})",
                    request.getStartFloor(), fixedStartFloor, request.getEndFloor(), fixedEndFloor);
        }

        boolean isSameFloor = (fixedStartFloor == fixedEndFloor);

        if (isSameFloor) {
            log.info("단일 층 경로 탐색 모드 실행 (Floor: {})", fixedStartFloor);
            return findPathOnSameFloor(
                    venueId,
                    request.getStartX(), request.getStartY(),
                    request.getEndX(), request.getEndY(),
                    fixedStartFloor
            );
        }

        log.info("다중 층 경로 탐색 모드 실행 ({} -> {})", fixedStartFloor, fixedEndFloor);
        // 다중 층 로직을 위해 Request 객체를 새로 생성하거나 값을 전달해야 합니다.
        // 여기서는 편의상 메서드 오버로딩을 활용하거나 필요한 값을 직접 넘기는 방식으로 수정 제안
        return findPathAcrossFloors(venueId, request.getStartX(), request.getStartY(), fixedStartFloor,
                request.getEndX(), request.getEndY(), fixedEndFloor);
    }

    // (오버로딩) 간편 호출용
    public PathFindingResponse findPath(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        PathFindingRequest request = PathFindingRequest.builder()
                .startX(startX).startY(startY).startFloor(startFloor)
                .endX(endX).endY(endY).endFloor(endFloor)
                .build();
        return findPath(venueId, request);
    }

    public PathFindingResponse findPathToFacility(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            Long facilityId
    ) {
        VenueFacility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> GeneralException.notFound("시설물을 찾을 수 없습니다."));

        return findPath(
                venueId,
                startX, startY, startFloor,
                facility.getX(), facility.getY(), facility.getFloor()
        );
    }

    // =================================================================================
    // [신규 기능] 층 보정 로직
    // =================================================================================
    private Integer validateAndCorrectFloor(Long venueId, BigDecimal x, BigDecimal y, Integer inputFloor) {
        // 1. 요청된 층이 유효한지 먼저 검사
        if (isPointOnFloor(venueId, x, y, inputFloor)) {
            return inputFloor;
        }

        log.warn("⚠️ 좌표({}, {})가 {}층 맵 범위를 벗어났거나 유효하지 않습니다. 다른 층을 탐색합니다.", x, y, inputFloor);

        // 2. 다른 층들을 순회하며 해당 좌표가 유효한 층을 찾음
        // (성능 최적화를 위해 DB에서 venueId의 존재하는 층 목록만 가져오는 것이 좋음)
        for (int floor = MIN_SEARCH_FLOOR; floor <= MAX_SEARCH_FLOOR; floor++) {
            if (floor == inputFloor) continue;

            if (isPointOnFloor(venueId, x, y, floor)) {
                log.info("✅ 좌표({}, {})가 {}층에서 발견되었습니다! 층 정보를 수정합니다.", x, y, floor);
                return floor;
            }
        }

        // 3. 어디에서도 찾지 못했다면 원래 요청 층 반환 (혹은 에러 처리)
        log.error("❌ 좌표({}, {})가 어떤 층의 구역에도 포함되지 않습니다. 가장 가까운 벽으로 매핑될 수 있습니다.", x, y);
        return inputFloor;
    }

    private boolean isPointOnFloor(Long venueId, BigDecimal x, BigDecimal y, Integer floor) {
        List<VenueSection> sections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);
        if (sections.isEmpty()) return false;

        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(x.doubleValue(), y.doubleValue()));

        // 섹션들을 다각형으로 변환하여 포함 여부 확인
        for (VenueSection section : sections) {
            try {
                // 섹션의 버텍스가 유효한지 간단 체크
                if (section.getVerticesList() == null || section.getVerticesList().size() < 3) continue;

                Polygon polygon = createPolygon(section.getVerticesList());
                // contains: 내부, intersects: 경계 포함 접촉 (여유 있게 intersects 사용 가능)
                if (polygon.intersects(point)) {
                    return true;
                }
            } catch (Exception e) {
                // 변환 실패 섹션은 무시
            }
        }
        return false;
    }

    // =================================================================================
    // 경로 탐색 로직 (단일 층)
    // =================================================================================
    private PathFindingResponse findPathOnSameFloor(
            Long venueId,
            BigDecimal startX, BigDecimal startY,
            BigDecimal endX, BigDecimal endY,
            Integer floor
    ) {
        List<VenueSection> sections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);

        // 이동 불가능한 섹션만 장애물로 간주
        List<VenueSection> obstacleSections = sections.stream()
                .filter(s -> {
                    if (s.getType() == null) return true;
                    return s.getType() == com.umc.connext.common.enums.SectionType.WALL ||
                            s.getType() == com.umc.connext.common.enums.SectionType.SEAT ||
                            s.getType() == com.umc.connext.common.enums.SectionType.STAGE ||
                            s.getType() == com.umc.connext.common.enums.SectionType.UNKNOWN;
                })
                .collect(Collectors.toList());

        List<Polygon> obstacles = convertToJTSPolygons(obstacleSections);

        org.locationtech.jts.geom.Coordinate rawStart = new org.locationtech.jts.geom.Coordinate(startX.doubleValue(), startY.doubleValue());
        org.locationtech.jts.geom.Coordinate rawEnd = new org.locationtech.jts.geom.Coordinate(endX.doubleValue(), endY.doubleValue());

        // 가장 가까운 이동 가능 좌표 찾기
        org.locationtech.jts.geom.Coordinate validStart = findNearestWalkablePoint(rawStart, obstacles);
        org.locationtech.jts.geom.Coordinate validEnd = findNearestWalkablePoint(rawEnd, obstacles);

        if (validStart == null || validEnd == null) {
            return PathFindingResponse.fail("출발지 또는 도착지가 이동 불가능한 영역에 있으며, 근처에 진입 가능한 경로가 없습니다.");
        }

        // A* 실행
        List<org.locationtech.jts.geom.Coordinate> pathCoords = aStarSearch(validStart, validEnd, obstacles);

        if (pathCoords.isEmpty()) {
            return PathFindingResponse.fail("경로를 찾을 수 없습니다 (장애물로 완전히 막혀있음)");
        }

        // 경로 스무딩
        pathCoords = smoothPath(pathCoords, obstacles);

        // 결과 변환
        List<Coordinate> coordinates = pathCoords.stream()
                .map(c -> new Coordinate(
                        BigDecimal.valueOf(c.x).setScale(1, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(c.y).setScale(1, RoundingMode.HALF_UP),
                        floor
                ))
                .collect(Collectors.toList());

        BigDecimal totalDistance = calculateTotalDistance(coordinates);
        return PathFindingResponse.success(coordinates, totalDistance, floor);
    }

    // =================================================================================
    // 경로 탐색 로직 (다중 층)
    // =================================================================================
    // 파라미터 수정: Request 객체 대신 명시적인 층 정보를 받도록 변경하여 보정된 값을 사용
    private PathFindingResponse findPathAcrossFloors(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        List<VenueFacility> stairs = facilityRepository.findStairsConnectingFloors(venueId, startFloor, endFloor);

        if (stairs.isEmpty()) {
            return PathFindingResponse.fail("두 층을 연결하는 계단/엘리베이터가 없습니다.");
        }

        // 최적 계단 선택 (출발지 -> 계단 + 계단 -> 도착지 거리 합이 최소인 것)
        VenueFacility bestStairs = stairs.stream().min(Comparator.comparingDouble(s -> {
            double d1 = calculateDistance(startX.doubleValue(), startY.doubleValue(), s.getX().doubleValue(), s.getY().doubleValue());
            double d2 = calculateDistance(s.getX().doubleValue(), s.getY().doubleValue(), endX.doubleValue(), endY.doubleValue());
            return d1 + d2;
        })).orElse(null);

        if (bestStairs == null) return PathFindingResponse.fail("이동 가능한 계단을 찾을 수 없습니다.");

        // 1. 출발층 경로 (출발지 -> 계단)
        PathFindingResponse pathToStairs = findPathOnSameFloor(venueId, startX, startY, bestStairs.getX(), bestStairs.getY(), startFloor);
        if (!pathToStairs.isSuccess()) return PathFindingResponse.fail("출발지에서 계단까지의 경로를 찾을 수 없습니다.");

        // 2. 도착층 경로 (계단 -> 도착지)
        PathFindingResponse pathFromStairs = findPathOnSameFloor(venueId, bestStairs.getX(), bestStairs.getY(), endX, endY, endFloor);
        if (!pathFromStairs.isSuccess()) return PathFindingResponse.fail("계단에서 도착지까지의 경로를 찾을 수 없습니다.");

        return mergePathsWithStairs(pathToStairs, pathFromStairs, bestStairs, startFloor, endFloor);
    }

    // =================================================================================
    // 유틸리티 및 A* 알고리즘
    // =================================================================================

    private org.locationtech.jts.geom.Coordinate findNearestWalkablePoint(org.locationtech.jts.geom.Coordinate target, List<Polygon> obstacles) {
        if (!isColliding(target, obstacles)) return target;

        // BFS로 가장 가까운 빈 공간 탐색
        int maxSteps = 20; // 탐색 범위 확장
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

                // 점 충돌 검사만 수행 (경로 검사는 제외, 일단 빈 공간만 찾으면 됨)
                if (!isColliding(next, obstacles)) {
                    return next;
                }
                visited.add(key);
                queue.add(next);
            }
        }
        return null; // 주변에 빈 공간 없음
    }

    private List<org.locationtech.jts.geom.Coordinate> aStarSearch(org.locationtech.jts.geom.Coordinate start, org.locationtech.jts.geom.Coordinate end, List<Polygon> obstacles) {
        if (isColliding(start, obstacles)) return Collections.emptyList();

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, start.distance(end));
        openList.add(startNode);
        allNodes.put(getKey(start), startNode);

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        int iterations = 0;

        while (!openList.isEmpty()) {
            if (iterations++ > MAX_ASTAR_ITERATIONS) break;

            Node current = openList.poll();
            String currentKey = getKey(current.coord);

            if (closedSet.contains(currentKey)) continue;
            closedSet.add(currentKey);

            if (current.coord.distance(end) < GRID_SIZE) return reconstructPath(current, end);

            for (int[] dir : directions) {
                double newX = current.coord.x + (dir[0] * GRID_SIZE);
                double newY = current.coord.y + (dir[1] * GRID_SIZE);
                org.locationtech.jts.geom.Coordinate neighborCoord = new org.locationtech.jts.geom.Coordinate(newX, newY);
                String neighborKey = getKey(neighborCoord);

                if (closedSet.contains(neighborKey)) continue;
                if (isColliding(neighborCoord, obstacles)) continue;
                if (isPathColliding(current.coord, neighborCoord, obstacles)) continue;

                // 대각선 이동 시 벽 긁기 방지 로직 (기존 유지)
                if (dir[0] != 0 && dir[1] != 0) {
                    org.locationtech.jts.geom.Coordinate c1 = new org.locationtech.jts.geom.Coordinate(current.coord.x + dir[0] * GRID_SIZE, current.coord.y);
                    org.locationtech.jts.geom.Coordinate c2 = new org.locationtech.jts.geom.Coordinate(current.coord.x, current.coord.y + dir[1] * GRID_SIZE);
                    if (isColliding(c1, obstacles) || isColliding(c2, obstacles)) continue;
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

    private List<Polygon> convertToJTSPolygons(List<VenueSection> sections) {
        if (sections == null || sections.isEmpty()) return new ArrayList<>();
        List<Polygon> polygons = new ArrayList<>();

        for (VenueSection section : sections) {
            List<Coordinate> vertices = section.getVerticesList();
            if (vertices == null || vertices.size() < 3) continue;
            try {
                polygons.add(createPolygon(vertices));
            } catch (Exception e) {
                log.error("Polygon 변환 실패 SectionId: {}", section.getSectionId());
            }
        }
        return polygons;
    }

    private Polygon createPolygon(List<Coordinate> vertices) {
        org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++) {
            jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(vertices.get(i).getX().doubleValue(), vertices.get(i).getY().doubleValue());
        }
        jtsCoords[vertices.size()] = jtsCoords[0]; // 닫힌 루프
        return geometryFactory.createPolygon(geometryFactory.createLinearRing(jtsCoords));
    }

    private List<org.locationtech.jts.geom.Coordinate> smoothPath(List<org.locationtech.jts.geom.Coordinate> path, List<Polygon> obstacles) {
        if (path.size() <= 2) return path;
        List<org.locationtech.jts.geom.Coordinate> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
        int i = 0;
        while (i < path.size() - 1) {
            int j = path.size() - 1;
            while (j > i + 1 && isPathColliding(path.get(i), path.get(j), obstacles)) {
                j--;
            }
            if (j == i + 1) {
                smoothed.add(path.get(i + 1));
                i++;
            } else {
                smoothed.add(path.get(j));
                i = j;
            }
        }
        return smoothed;
    }

    private boolean isColliding(org.locationtech.jts.geom.Coordinate coord, List<Polygon> obstacles) {
        Point point = geometryFactory.createPoint(coord);
        for (Polygon polygon : obstacles) {
            if (polygon.intersects(point)) return true;
        }
        return false;
    }

    private boolean isPathColliding(org.locationtech.jts.geom.Coordinate from, org.locationtech.jts.geom.Coordinate to, List<Polygon> obstacles) {
        if (isColliding(from, obstacles) || isColliding(to, obstacles)) return true;
        LineString path = geometryFactory.createLineString(new org.locationtech.jts.geom.Coordinate[]{from, to});
        for (Polygon obstacle : obstacles) {
            if (path.intersects(obstacle)) return true;
        }
        return false;
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

    private BigDecimal calculateTotalDistance(List<Coordinate> path) {
        if (path == null || path.size() < 2) return BigDecimal.ZERO;
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) total += path.get(i).distanceTo(path.get(i + 1));
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private PathFindingResponse mergePathsWithStairs(PathFindingResponse p1, PathFindingResponse p2, VenueFacility stairs, int f1, int f2) {
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

    private String getKey(org.locationtech.jts.geom.Coordinate c) {
        return String.format("%.1f,%.1f", c.x, c.y);
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