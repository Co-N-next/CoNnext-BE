package com.umc.connext.domain.venue.service;

import com.umc.connext.common.enums.DirectionType;
import com.umc.connext.common.enums.SectionType;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.venue.dto.*;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueFacilityRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PathFindingService {

    private final VenueSectionRepository sectionRepository;
    private final VenueFacilityRepository facilityRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private static final double GRID_SIZE = 10.0;
    private static final int MAX_ASTAR_ITERATIONS = 20000;
    private static final int MIN_SEARCH_FLOOR = -2;
    private static final int MAX_SEARCH_FLOOR = 10;

    /**
     * 메인 경로 탐색 메서드
     */
    public PathFindingResponse findPath(Long venueId, PathFindingRequest request) {
        log.info("Pathfinding request: start(floor={}, x={}, y={}), end(floor={}, x={}, y={}), guide={}",
                request.getStartFloor(), request.getStartX(), request.getStartY(),
                request.getEndFloor(), request.getEndX(), request.getEndY(), request.isIncludeGuide());

        // 층 정보 보정 (사용자가 잘못된 층 정보를 보냈을 경우 보정)
        int fixedStartFloor = validateAndCorrectFloor(venueId, request.getStartX(), request.getStartY(), request.getStartFloor());
        int fixedEndFloor = validateAndCorrectFloor(venueId, request.getEndX(), request.getEndY(), request.getEndFloor());

        PathFindingResponse response;

        // 같은 층 vs 다른 층 분기 처리
        if (fixedStartFloor == fixedEndFloor) {
            response = findPathOnSameFloor(
                    venueId,
                    request.getStartX(), request.getStartY(),
                    request.getEndX(), request.getEndY(),
                    fixedStartFloor
            );
        } else {
            response = findPathAcrossFloors(
                    venueId,
                    request.getStartX(), request.getStartY(), fixedStartFloor,
                    request.getEndX(), request.getEndY(), fixedEndFloor
            );
        }

        // (옵션) 상세 안내 가이드 생성
        // 경로 찾기에 성공했고, 사용자가 가이드를 요청했을 때만 수행
        if (response.isSuccess() && request.isIncludeGuide()) {
            List<NavigationStep> steps = generateNavigationSteps(response.getCoordinates(), response.getFloorTransitions());

            // 기존 응답에 가이드 추가하여 재빌드
            return PathFindingResponse.builder()
                    .coordinates(response.getCoordinates())
                    .totalDistance(response.getTotalDistance())
                    .startFloor(response.getStartFloor())
                    .endFloor(response.getEndFloor())
                    .floorTransitions(response.getFloorTransitions())
                    .navigationSteps(steps) // 가이드 추가
                    .build();
        }

        return response;
    }

    /**
     * GET 요청용 오버로딩 (Controller에서 호출)
     */
    public PathFindingResponse findPath(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        // 기본적으로 가이드 없이 요청 (GET 파라미터 처리는 Controller에서 수행 권장)
        PathFindingRequest request = PathFindingRequest.builder()
                .startX(startX).startY(startY).startFloor(startFloor)
                .endX(endX).endY(endY).endFloor(endFloor)
                .includeGuide(false)
                .build();
        return findPath(venueId, request);
    }

    /**
     * 시설물까지 경로 찾기
     */
    public PathFindingResponse findPathToFacility(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            Long facilityId, boolean includeGuide
    ) {
        VenueFacility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> GeneralException.notFound("시설물을 찾을 수 없습니다."));

        PathFindingRequest request = PathFindingRequest.builder()
                .startX(startX).startY(startY).startFloor(startFloor)
                .endX(facility.getX()).endY(facility.getY()).endFloor(facility.getFloor())
                .includeGuide(includeGuide)
                .build();

        return findPath(venueId, request);
    }

    private PathFindingResponse findPathOnSameFloor(
            Long venueId,
            BigDecimal startX, BigDecimal startY,
            BigDecimal endX, BigDecimal endY,
            Integer floor
    ) {
        log.info("=== PathFinding Debug: Floor {} ===", floor);

        List<VenueSection> currentFloorSections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);
        log.info("Current floor sections: {}", currentFloorSections.size());

        currentFloorSections.forEach(s ->
            log.debug("Section ID: {}, Type: {}, Vertices: {}",
                s.getSectionId(), s.getType(),
                s.getVerticesList() != null ? s.getVerticesList().size() : 0)
        );

        List<VenueSection> obstacleSections = currentFloorSections.stream()
                .filter(s -> {
                    if (s.getType() == null) return true;
                    return s.getType() == SectionType.WALL
                            || s.getType() == SectionType.SEAT
                            || s.getType() == SectionType.STAGE
                            || s.getType() == SectionType.UNKNOWN;
                })
                .collect(Collectors.toList());

        log.info("Obstacle sections after filtering: {}", obstacleSections.size());

        List<VenueSection> otherFloorSections = sectionRepository.findAllByVenueId(venueId).stream()
                .filter(s -> !floor.equals(s.getFloor()))
                .collect(Collectors.toList());

        log.info("Other floor sections added as obstacles: {}", otherFloorSections.size());
        obstacleSections.addAll(otherFloorSections);

        log.info("Total obstacle sections: {}", obstacleSections.size());

        List<Polygon> obstacles = convertToJTSPolygons(obstacleSections);
        log.info("Successfully converted polygons: {}", obstacles.size());

        if (obstacles.isEmpty()) {
            log.warn("No obstacles found on floor {}. Path may be direct line.", floor);
        }

        // 시작점/도착점 보정 (장애물 내부에 있을 경우 가장 가까운 이동 가능 좌표로 이동)
        org.locationtech.jts.geom.Coordinate rawStart =
                new org.locationtech.jts.geom.Coordinate(startX.doubleValue(), startY.doubleValue());
        org.locationtech.jts.geom.Coordinate rawEnd =
                new org.locationtech.jts.geom.Coordinate(endX.doubleValue(), endY.doubleValue());

        log.info("Raw start: ({}, {}), Raw end: ({}, {})", rawStart.x, rawStart.y, rawEnd.x, rawEnd.y);

        org.locationtech.jts.geom.Coordinate validStart = findNearestWalkablePoint(rawStart, obstacles);
        org.locationtech.jts.geom.Coordinate validEnd = findNearestWalkablePoint(rawEnd, obstacles);

        log.info("Valid start: {}, Valid end: {}", validStart != null, validEnd != null);
        if (validStart != null && validEnd != null) {
            log.info("Adjusted start: ({}, {}), Adjusted end: ({}, {})",
                validStart.x, validStart.y, validEnd.x, validEnd.y);
        }

        if (validStart == null || validEnd == null) {
            return PathFindingResponse.fail("출발지 또는 도착지가 이동 불가능한 영역에 있으며, 근처에 진입 가능한 경로가 없습니다.");
        }

        // A* 알고리즘 수행
        List<org.locationtech.jts.geom.Coordinate> pathCoords = aStarSearch(validStart, validEnd, obstacles);

        log.info("A* search returned path with {} coordinates", pathCoords.size());

        if (pathCoords.isEmpty()) {
            return PathFindingResponse.fail("경로를 찾을 수 없습니다 (장애물로 완전히 막혀있음)");
        }

        // 경로 부드럽게 만들기 (직선화)
        List<org.locationtech.jts.geom.Coordinate> smoothedPath = smoothPath(pathCoords, obstacles);
        log.info("Path smoothing: {} coordinates -> {} coordinates", pathCoords.size(), smoothedPath.size());

        // 결과 변환
        List<Coordinate> coordinates = smoothedPath.stream()
                .map(c -> new Coordinate(
                        BigDecimal.valueOf(c.x).setScale(1, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(c.y).setScale(1, RoundingMode.HALF_UP),
                        floor
                ))
                .collect(Collectors.toList());

        BigDecimal totalDistance = calculateTotalDistance(coordinates);
        log.info("Final path distance: {}", totalDistance);

        return PathFindingResponse.success(coordinates, totalDistance, floor);
    }

    private PathFindingResponse findPathAcrossFloors(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        // 두 층을 연결하는 수단(계단/엘리베이터) 조회
        List<VenueFacility> stairs = facilityRepository.findStairsConnectingFloors(venueId, startFloor, endFloor);
        if (stairs.isEmpty()) {
            return PathFindingResponse.fail("두 층을 연결하는 계단/엘리베이터가 없습니다.");
        }

        // 가장 효율적인(가까운) 계단 찾기
        VenueFacility bestStairs = stairs.stream().min(Comparator.comparingDouble(s -> {
            double d1 = calculateDistance(startX.doubleValue(), startY.doubleValue(), s.getX().doubleValue(), s.getY().doubleValue());
            double d2 = calculateDistance(s.getX().doubleValue(), s.getY().doubleValue(), endX.doubleValue(), endY.doubleValue());
            return d1 + d2;
        })).orElse(null);

        if (bestStairs == null) return PathFindingResponse.fail("이동 가능한 계단을 찾을 수 없습니다.");

        // 경로 쪼개서 계산 (Start -> 계단, 계단 -> End)
        PathFindingResponse pathToStairs = findPathOnSameFloor(venueId, startX, startY, bestStairs.getX(), bestStairs.getY(), startFloor);
        if (!pathToStairs.isSuccess()) return PathFindingResponse.fail("출발지에서 계단까지의 경로를 찾을 수 없습니다.");

        PathFindingResponse pathFromStairs = findPathOnSameFloor(venueId, bestStairs.getX(), bestStairs.getY(), endX, endY, endFloor);
        if (!pathFromStairs.isSuccess()) return PathFindingResponse.fail("계단에서 도착지까지의 경로를 찾을 수 없습니다.");

        // 두 경로 합치기
        return mergePathsWithStairs(pathToStairs, pathFromStairs, bestStairs, startFloor, endFloor);
    }

    private List<NavigationStep> generateNavigationSteps(List<Coordinate> path, List<FloorTransition> transitions) {
        if (path == null || path.size() < 2) return Collections.emptyList();

        List<NavigationStep> steps = new ArrayList<>();
        int stepOrder = 1;

        // 출발
        steps.add(NavigationStep.builder()
                .stepOrder(stepOrder++)
                .description("경로 안내를 시작합니다.")
                .type(DirectionType.START)
                .coordinate(path.get(0))
                .distance(BigDecimal.ZERO)
                .build());

        double accumulatedDist = 0;
        int lastTurnIndex = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Coordinate curr = path.get(i);
            Coordinate next = path.get(i + 1);

            // 거리 누적
            double dist = curr.distanceTo(next);
            accumulatedDist += dist;

            // [Case A] 층 변경 감지 (계단/엘리베이터)
            if (!curr.getFloor().equals(next.getFloor())) {
                // 직진해온 거리가 있다면 먼저 추가
                if (accumulatedDist > 0) {
                    addStraightStep(steps, stepOrder++, accumulatedDist, path.get(lastTurnIndex));
                    accumulatedDist = 0;
                }

                boolean isUp = next.getFloor() > curr.getFloor();
                String transName = findTransitionName(i, transitions);
                steps.add(NavigationStep.builder()
                        .stepOrder(stepOrder++)
                        .description(String.format("%s을(를) 통해 %d층으로 이동하세요.", transName, next.getFloor()))
                        .type(isUp ? DirectionType.STAIRS_UP : DirectionType.STAIRS_DOWN)
                        .coordinate(curr)
                        .distance(BigDecimal.ZERO)
                        .build());

                lastTurnIndex = i + 1;
                continue; // 층 이동 시 방향 계산 건너뜀
            }

            // [Case B] 방향 전환 감지 (다음 점이 존재할 때)
            if (i + 2 < path.size()) {
                Coordinate nextNext = path.get(i + 2);

                // 같은 층 내에서만 각도 계산
                if (next.getFloor().equals(nextNext.getFloor())) {
                    double angle = calculateTurnAngle(curr, next, nextNext);
                    DirectionType turnType = getDirectionFromAngle(angle);

                    // 회전이 발생하면
                    if (turnType != DirectionType.STRAIGHT) {
                        // 지금까지의 직진 거리 안내 (2m 이상일 때만)
                        if (accumulatedDist >= 2.0) {
                            addStraightStep(steps, stepOrder++, accumulatedDist, path.get(lastTurnIndex));
                        }

                        // 회전 안내
                        steps.add(NavigationStep.builder()
                                .stepOrder(stepOrder++)
                                .description(String.format("%s하세요.", getTurnDescription(turnType)))
                                .type(turnType)
                                .coordinate(next)
                                .distance(BigDecimal.ZERO)
                                .build());

                        accumulatedDist = 0;
                        lastTurnIndex = i + 1;
                    }
                }
            }
        }

        // 마지막 남은 직진 거리 추가
        if (accumulatedDist > 0) {
            addStraightStep(steps, stepOrder++, accumulatedDist, path.get(lastTurnIndex));
        }

        // 도착
        steps.add(NavigationStep.builder()
                .stepOrder(stepOrder)
                .description("목적지에 도착했습니다.")
                .type(DirectionType.ARRIVE)
                .coordinate(path.get(path.size() - 1))
                .distance(BigDecimal.ZERO)
                .build());

        return steps;
    }

    private void addStraightStep(List<NavigationStep> steps, int order, double distance, Coordinate coord) {
        steps.add(NavigationStep.builder()
                .stepOrder(order)
                .description(String.format("약 %.0fm 직진하세요.", distance))
                .type(DirectionType.STRAIGHT)
                .coordinate(coord)
                .distance(BigDecimal.valueOf(distance).setScale(1, RoundingMode.HALF_UP))
                .build());
    }

    private double calculateTurnAngle(Coordinate p1, Coordinate p2, Coordinate p3) {
        double angle1 = Math.atan2(p2.getY().doubleValue() - p1.getY().doubleValue(),
                p2.getX().doubleValue() - p1.getX().doubleValue());
        double angle2 = Math.atan2(p3.getY().doubleValue() - p2.getY().doubleValue(),
                p3.getX().doubleValue() - p2.getX().doubleValue());

        double angleDiff = Math.toDegrees(angle2 - angle1);
        if (angleDiff > 180) angleDiff -= 360;
        if (angleDiff < -180) angleDiff += 360;
        return angleDiff;
    }

    private DirectionType getDirectionFromAngle(double angle) {
        if (angle > -30 && angle < 30) return DirectionType.STRAIGHT;
        if (angle >= 30 && angle < 60) return DirectionType.SLIGHT_RIGHT;
        if (angle >= 60 && angle < 120) return DirectionType.RIGHT;
        if (angle <= -30 && angle > -60) return DirectionType.SLIGHT_LEFT;
        if (angle <= -60 && angle > -120) return DirectionType.LEFT;
        return DirectionType.STRAIGHT;
    }

    private String getTurnDescription(DirectionType type) {
        switch (type) {
            case LEFT: return "좌회전";
            case RIGHT: return "우회전";
            case SLIGHT_LEFT: return "왼쪽 방향으로 이동";
            case SLIGHT_RIGHT: return "오른쪽 방향으로 이동";
            default: return "이동";
        }
    }

    private String findTransitionName(int index, List<FloorTransition> transitions) {
        if (transitions == null) return "계단";
        for (FloorTransition t : transitions) {
            // 경로 인덱스와 근접한 트랜지션 찾기
            if (Math.abs(t.getPathIndex() - index) <= 1) {
                return t.getStairsName() != null ? t.getStairsName() : "계단";
            }
        }
        return "계단";
    }

    private Integer validateAndCorrectFloor(Long venueId, BigDecimal x, BigDecimal y, Integer inputFloor) {
        if (isPointOnFloor(venueId, x, y, inputFloor)) {
            return inputFloor;
        }
        log.warn("Point out of floor range: x={}, y={}, floor={}. Searching other floors.", x, y, inputFloor);
        for (int floor = MIN_SEARCH_FLOOR; floor <= MAX_SEARCH_FLOOR; floor++) {
            if (floor == inputFloor) continue;
            if (isPointOnFloor(venueId, x, y, floor)) {
                return floor;
            }
        }
        return inputFloor;
    }

    private boolean isPointOnFloor(Long venueId, BigDecimal x, BigDecimal y, Integer floor) {
        List<VenueSection> sections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);
        if (sections.isEmpty()) return false;
        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(x.doubleValue(), y.doubleValue()));
        for (VenueSection section : sections) {
            try {
                if (section.getVerticesList() == null || section.getVerticesList().size() < 3) continue;
                Polygon polygon = createPolygon(section.getVerticesList());
                if (polygon.intersects(point)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private org.locationtech.jts.geom.Coordinate findNearestWalkablePoint(org.locationtech.jts.geom.Coordinate target, List<Polygon> obstacles) {
        if (!isColliding(target, obstacles)) {
            log.debug("Target ({}, {}) is already walkable", target.x, target.y);
            return target;
        }

        log.debug("Target ({}, {}) is in collision, searching for nearest walkable point", target.x, target.y);
        int maxSteps = 20;
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
                if (!isColliding(next, obstacles)) {
                    log.debug("Found walkable point: ({}, {}) at distance {}", next.x, next.y, next.distance(target));
                    return next;
                }
                visited.add(key);
                queue.add(next);
            }
        }
        log.warn("No walkable point found within search radius for target ({}, {})", target.x, target.y);
        return null;
    }

    private List<org.locationtech.jts.geom.Coordinate> aStarSearch(org.locationtech.jts.geom.Coordinate start, org.locationtech.jts.geom.Coordinate end, List<Polygon> obstacles) {
        if (isColliding(start, obstacles)) {
            log.warn("A* Search: Start point is in collision");
            return Collections.emptyList();
        }

        log.debug("A* Search started: start({}, {}), end({}, {}), obstacles={}",
            start.x, start.y, end.x, end.y, obstacles.size());
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
        log.warn("A* Search: No path found after {} iterations", MAX_ASTAR_ITERATIONS);
        return Collections.emptyList();
    }

    private List<Polygon> convertToJTSPolygons(List<VenueSection> sections) {
        if (sections == null || sections.isEmpty()) {
            log.warn("No sections provided for polygon conversion");
            return new ArrayList<>();
        }
        List<Polygon> polygons = new ArrayList<>();
        for (VenueSection section : sections) {
            List<Coordinate> vertices = section.getVerticesList();
            if (vertices == null) {
                log.debug("Section ID: {} has null vertices", section.getSectionId());
                continue;
            }
            if (vertices.size() < 3) {
                log.debug("Section ID: {} has insufficient vertices ({}), skipping", section.getSectionId(), vertices.size());
                continue;
            }
            try {
                Polygon polygon = createPolygon(vertices);
                polygons.add(polygon);
                log.debug("Successfully created polygon for section ID: {}, vertices: {}, area: {}",
                    section.getSectionId(), vertices.size(), polygon.getArea());
            } catch (Exception e) {
                log.error("Failed to convert section to polygon. sectionId={}, error={}", section.getSectionId(), e.getMessage());
            }
        }
        log.info("Polygon conversion complete: {} sections -> {} valid polygons", sections.size(), polygons.size());
        return polygons;
    }

    private Polygon createPolygon(List<Coordinate> vertices) {
        org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++) {
            jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(vertices.get(i).getX().doubleValue(), vertices.get(i).getY().doubleValue());
        }
        jtsCoords[vertices.size()] = jtsCoords[0];
        return geometryFactory.createPolygon(geometryFactory.createLinearRing(jtsCoords));
    }

    private List<org.locationtech.jts.geom.Coordinate> smoothPath(List<org.locationtech.jts.geom.Coordinate> path, List<Polygon> obstacles) {
        if (path.size() <= 2) return path;
        List<org.locationtech.jts.geom.Coordinate> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
        int i = 0;
        int iteration = 0;

        log.debug("=== Path Smoothing Debug ===");
        log.debug("Initial path size: {}, obstacles count: {}", path.size(), obstacles.size());

        while (i < path.size() - 1) {
            int j = path.size() - 1;
            int originalJ = j;

            while (j > i + 1 && isPathColliding(path.get(i), path.get(j), obstacles)) {
                j--;
            }

            log.debug("Iteration {}: i={}, originalJ={}, finalJ={}, collision={}",
                iteration, i, originalJ, j, originalJ != j);
            log.debug("  Checking path from ({}, {}) to ({}, {})",
                path.get(i).x, path.get(i).y, path.get(j).x, path.get(j).y);

            if (j == i + 1) {
                smoothed.add(path.get(i + 1));
                i++;
                log.debug("  -> No shortcut possible, adding next point");
            } else {
                smoothed.add(path.get(j));
                i = j;
                log.debug("  -> Shortcut found, jumping to position {}", j);
            }
            iteration++;
        }

        log.debug("Path smoothing complete: {} -> {} coordinates", path.size(), smoothed.size());
        return smoothed;
    }

    private boolean isColliding(org.locationtech.jts.geom.Coordinate coord, List<Polygon> obstacles) {
        if (obstacles.isEmpty()) return false;

        Point point = geometryFactory.createPoint(coord);
        for (Polygon polygon : obstacles) {
            if (polygon.intersects(point)) {
                log.trace("Point ({}, {}) collides with obstacle (area: {})",
                    coord.x, coord.y, polygon.getArea());
                return true;
            }
        }
        return false;
    }

    private boolean isPathColliding(org.locationtech.jts.geom.Coordinate from, org.locationtech.jts.geom.Coordinate to, List<Polygon> obstacles) {
        if (isColliding(from, obstacles) || isColliding(to, obstacles)) {
            log.debug("Path collision detected at endpoint: from({}, {}), to({}, {})",
                from.x, from.y, to.x, to.y);
            return true;
        }
        LineString path = geometryFactory.createLineString(new org.locationtech.jts.geom.Coordinate[]{from, to});
        for (Polygon obstacle : obstacles) {
            if (path.intersects(obstacle)) {
                log.debug("LineString collision detected: from({}, {}), to({}, {})",
                    from.x, from.y, to.x, to.y);
                return true;
            }
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
        for (int i = 0; i < path.size() - 1; i++) {
            total += path.get(i).distanceTo(path.get(i + 1));
        }
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
        // 여기서도 NavigationStep을 바로 만들지 않고 데이터만 리턴 (메인 findPath에서 처리)
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