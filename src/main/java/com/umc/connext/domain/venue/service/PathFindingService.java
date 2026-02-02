package com.umc.connext.domain.venue.service;

import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.enums.SectionType;
import com.umc.connext.domain.venue.dto.Coordinate;
import com.umc.connext.domain.venue.dto.FloorTransition;
import com.umc.connext.domain.venue.dto.PathFindingRequest;
import com.umc.connext.domain.venue.dto.PathFindingResponse;
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
    private static final int MIN_SEARCH_FLOOR = -2;
    private static final int MAX_SEARCH_FLOOR = 10;

    public PathFindingResponse findPath(Long venueId, PathFindingRequest request) {
        log.info("Pathfinding request: start(floor={}, x={}, y={}), end(floor={}, x={}, y={})",
                request.getStartFloor(), request.getStartX(), request.getStartY(),
                request.getEndFloor(), request.getEndX(), request.getEndY());

        int fixedStartFloor = validateAndCorrectFloor(venueId, request.getStartX(), request.getStartY(), request.getStartFloor());
        int fixedEndFloor = validateAndCorrectFloor(venueId, request.getEndX(), request.getEndY(), request.getEndFloor());

        if (!Objects.equals(fixedStartFloor, request.getStartFloor()) || !Objects.equals(fixedEndFloor, request.getEndFloor())) {
            log.warn("Floor corrected: start({} -> {}), end({} -> {})",
                    request.getStartFloor(), fixedStartFloor, request.getEndFloor(), fixedEndFloor);
        }

        if (fixedStartFloor == fixedEndFloor) {
            return findPathOnSameFloor(
                    venueId,
                    request.getStartX(), request.getStartY(),
                    request.getEndX(), request.getEndY(),
                    fixedStartFloor
            );
        }

        return findPathAcrossFloors(
                venueId,
                request.getStartX(), request.getStartY(), fixedStartFloor,
                request.getEndX(), request.getEndY(), fixedEndFloor
        );
    }

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
        return findPath(venueId, startX, startY, startFloor, facility.getX(), facility.getY(), facility.getFloor());
    }

    private Integer validateAndCorrectFloor(Long venueId, BigDecimal x, BigDecimal y, Integer inputFloor) {
        if (isPointOnFloor(venueId, x, y, inputFloor)) {
            return inputFloor;
        }

        log.warn("Point out of floor range: x={}, y={}, floor={}. Searching other floors.", x, y, inputFloor);

        for (int floor = MIN_SEARCH_FLOOR; floor <= MAX_SEARCH_FLOOR; floor++) {
            if (floor == inputFloor) continue;
            if (isPointOnFloor(venueId, x, y, floor)) {
                log.info("Point found on another floor: x={}, y={}, floor={}", x, y, floor);
                return floor;
            }
        }

        log.error("Point not found on any floor: x={}, y={}", x, y);
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
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private PathFindingResponse findPathOnSameFloor(
            Long venueId,
            BigDecimal startX, BigDecimal startY,
            BigDecimal endX, BigDecimal endY,
            Integer floor
    ) {
        List<VenueSection> sections = sectionRepository.findAllByVenueIdAndFloor(venueId, floor);

        List<VenueSection> obstacleSections = sections.stream()
                .filter(s -> {
                    if (s.getType() == null) return true;
                    return s.getType() == SectionType.WALL
                            || s.getType() == SectionType.SEAT
                            || s.getType() == SectionType.STAGE
                            || s.getType() == SectionType.UNKNOWN;
                })
                .collect(Collectors.toList());

        List<Polygon> obstacles = convertToJTSPolygons(obstacleSections);

        org.locationtech.jts.geom.Coordinate rawStart =
                new org.locationtech.jts.geom.Coordinate(startX.doubleValue(), startY.doubleValue());
        org.locationtech.jts.geom.Coordinate rawEnd =
                new org.locationtech.jts.geom.Coordinate(endX.doubleValue(), endY.doubleValue());

        org.locationtech.jts.geom.Coordinate validStart = findNearestWalkablePoint(rawStart, obstacles);
        org.locationtech.jts.geom.Coordinate validEnd = findNearestWalkablePoint(rawEnd, obstacles);

        if (validStart == null || validEnd == null) {
            return PathFindingResponse.fail("출발지 또는 도착지가 이동 불가능한 영역에 있으며, 근처에 진입 가능한 경로가 없습니다.");
        }

        List<org.locationtech.jts.geom.Coordinate> pathCoords = aStarSearch(validStart, validEnd, obstacles);

        if (pathCoords.isEmpty()) {
            return PathFindingResponse.fail("경로를 찾을 수 없습니다 (장애물로 완전히 막혀있음)");
        }

        pathCoords = smoothPath(pathCoords, obstacles);

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

    private PathFindingResponse findPathAcrossFloors(
            Long venueId,
            BigDecimal startX, BigDecimal startY, Integer startFloor,
            BigDecimal endX, BigDecimal endY, Integer endFloor
    ) {
        List<VenueFacility> stairs = facilityRepository.findStairsConnectingFloors(venueId, startFloor, endFloor);
        if (stairs.isEmpty()) {
            return PathFindingResponse.fail("두 층을 연결하는 계단/엘리베이터가 없습니다.");
        }

        VenueFacility bestStairs = stairs.stream().min(Comparator.comparingDouble(s -> {
            double d1 = calculateDistance(startX.doubleValue(), startY.doubleValue(), s.getX().doubleValue(), s.getY().doubleValue());
            double d2 = calculateDistance(s.getX().doubleValue(), s.getY().doubleValue(), endX.doubleValue(), endY.doubleValue());
            return d1 + d2;
        })).orElse(null);

        if (bestStairs == null) return PathFindingResponse.fail("이동 가능한 계단을 찾을 수 없습니다.");

        PathFindingResponse pathToStairs = findPathOnSameFloor(venueId, startX, startY, bestStairs.getX(), bestStairs.getY(), startFloor);
        if (!pathToStairs.isSuccess()) return PathFindingResponse.fail("출발지에서 계단까지의 경로를 찾을 수 없습니다.");

        PathFindingResponse pathFromStairs = findPathOnSameFloor(venueId, bestStairs.getX(), bestStairs.getY(), endX, endY, endFloor);
        if (!pathFromStairs.isSuccess()) return PathFindingResponse.fail("계단에서 도착지까지의 경로를 찾을 수 없습니다.");

        return mergePathsWithStairs(pathToStairs, pathFromStairs, bestStairs, startFloor, endFloor);
    }

    private org.locationtech.jts.geom.Coordinate findNearestWalkablePoint(
            org.locationtech.jts.geom.Coordinate target,
            List<Polygon> obstacles
    ) {
        if (!isColliding(target, obstacles)) return target;

        int maxSteps = 20;
        Set<String> visited = new HashSet<>();
        Queue<org.locationtech.jts.geom.Coordinate> queue = new LinkedList<>();

        queue.add(target);
        visited.add(getKey(target));

        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

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
                    return next;
                }

                visited.add(key);
                queue.add(next);
            }
        }
        return null;
    }

    private List<org.locationtech.jts.geom.Coordinate> aStarSearch(
            org.locationtech.jts.geom.Coordinate start,
            org.locationtech.jts.geom.Coordinate end,
            List<Polygon> obstacles
    ) {
        if (isColliding(start, obstacles)) return Collections.emptyList();

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
            if (iterations++ > MAX_ASTAR_ITERATIONS) break;

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
                if (isColliding(neighborCoord, obstacles)) continue;
                if (isPathColliding(current.coord, neighborCoord, obstacles)) continue;

                if (dir[0] != 0 && dir[1] != 0) {
                    org.locationtech.jts.geom.Coordinate c1 =
                            new org.locationtech.jts.geom.Coordinate(current.coord.x + dir[0] * GRID_SIZE, current.coord.y);
                    org.locationtech.jts.geom.Coordinate c2 =
                            new org.locationtech.jts.geom.Coordinate(current.coord.x, current.coord.y + dir[1] * GRID_SIZE);
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
                log.error("Failed to convert section to polygon. sectionId={}", section.getSectionId());
            }
        }

        return polygons;
    }

    private Polygon createPolygon(List<Coordinate> vertices) {
        org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++) {
            jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(
                    vertices.get(i).getX().doubleValue(),
                    vertices.get(i).getY().doubleValue()
            );
        }
        jtsCoords[vertices.size()] = jtsCoords[0];
        return geometryFactory.createPolygon(geometryFactory.createLinearRing(jtsCoords));
    }

    private List<org.locationtech.jts.geom.Coordinate> smoothPath(
            List<org.locationtech.jts.geom.Coordinate> path,
            List<Polygon> obstacles
    ) {
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

    private boolean isPathColliding(
            org.locationtech.jts.geom.Coordinate from,
            org.locationtech.jts.geom.Coordinate to,
            List<Polygon> obstacles
    ) {
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
        for (int i = 0; i < path.size() - 1; i++) {
            total += path.get(i).distanceTo(path.get(i + 1));
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
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
