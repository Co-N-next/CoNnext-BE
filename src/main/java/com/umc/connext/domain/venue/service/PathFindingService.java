package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.dto.Coordinate;
import com.umc.connext.domain.venue.dto.PathFindingResponse;
import com.umc.connext.domain.venue.entity.VenueEdge;
import com.umc.connext.domain.venue.entity.VenueNode;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueEdgeRepository;
import com.umc.connext.domain.venue.repository.VenueNodeRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathFindingService {

    private final VenueNodeRepository nodeRepository;
    private final VenueEdgeRepository edgeRepository;
    private final VenueSectionRepository sectionRepository;

    /**
     * 임의의 두 좌표 사이의 최단 경로 찾기
     *
     * @param venueId 경기장 ID
     * @param startX 시작점 X 좌표
     * @param startY 시작점 Y 좌표
     * @param endX 도착점 X 좌표
     * @param endY 도착점 Y 좌표
     * @return 경로 정보
     */
    public PathFindingResponse findPath(Long venueId,
                                        BigDecimal startX, BigDecimal startY,
                                        BigDecimal endX, BigDecimal endY) {

        // 1. 시작점과 도착점에서 가장 가까운 노드 찾기
        VenueNode startNode = findNearestNode(venueId, startX, startY);
        VenueNode endNode = findNearestNode(venueId, endX, endY);

        if (startNode == null || endNode == null) {
            return PathFindingResponse.builder()
                    .path(Collections.emptyList())
                    .totalDistance(BigDecimal.ZERO)
                    .nodeCount(0)
                    .build();
        }

        // 2. Dijkstra 알고리즘으로 최단 경로 찾기
        List<VenueNode> graphPath = dijkstra(venueId, startNode.getNodeId(), endNode.getNodeId());

        // 3. 시작점/도착점까지의 직선 거리 추가
        BigDecimal startToNodeDistance = calculateDistance(startX, startY, startNode.getX(), startNode.getY());
        BigDecimal nodeToEndDistance = calculateDistance(endNode.getX(), endNode.getY(), endX, endY);

        // 4. 전체 경로 구성 (시작점 -> 첫 노드 -> ... -> 마지막 노드 -> 도착점)
        List<VenueNode> fullPath = new ArrayList<>();

        // 시작점을 가상 노드로 추가
        fullPath.add(VenueNode.builder()
                .nodeId(-1L)
                .venueId(venueId)
                .x(startX)
                .y(startY)
                .build());

        fullPath.addAll(graphPath);

        // 도착점을 가상 노드로 추가
        fullPath.add(VenueNode.builder()
                .nodeId(-2L)
                .venueId(venueId)
                .x(endX)
                .y(endY)
                .build());

        // 5. 총 거리 계산
        BigDecimal totalDistance = calculatePathDistance(graphPath)
                .add(startToNodeDistance)
                .add(nodeToEndDistance);

        return PathFindingResponse.builder()
                .path(fullPath)
                .totalDistance(totalDistance)
                .nodeCount(fullPath.size())
                .startNodeId(startNode.getNodeId())
                .endNodeId(endNode.getNodeId())
                .coordinates(fullPath.stream()
                        .map(node -> new Coordinate(node.getX(), node.getY()))
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 특정 좌표에서 가장 가까운 그래프 노드 찾기
     */
    private VenueNode findNearestNode(Long venueId, BigDecimal x, BigDecimal y) {
        List<VenueNode> allNodes = nodeRepository.findByVenueId(venueId);

        VenueNode nearestNode = null;
        BigDecimal minDistance = null;

        for (VenueNode node : allNodes) {
            BigDecimal distance = calculateDistance(x, y, node.getX(), node.getY());

            if (minDistance == null || distance.compareTo(minDistance) < 0) {
                minDistance = distance;
                nearestNode = node;
            }
        }

        return nearestNode;
    }

    /**
     * Dijkstra 알고리즘으로 최단 경로 찾기
     */
    private List<VenueNode> dijkstra(Long venueId, Long startNodeId, Long endNodeId) {
        // 모든 노드와 엣지 로드
        List<VenueNode> allNodes = nodeRepository.findByVenueId(venueId);
        List<VenueEdge> allEdges = edgeRepository.findByVenueId(venueId);

        // 노드 ID -> 노드 맵
        Map<Long, VenueNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(VenueNode::getNodeId, node -> node));

        // 인접 리스트 구성
        Map<Long, List<EdgeInfo>> adjacencyList = new HashMap<>();
        for (VenueEdge edge : allEdges) {
            adjacencyList.computeIfAbsent(edge.getNodeFrom(), k -> new ArrayList<>())
                    .add(new EdgeInfo(edge.getNodeTo(), edge.getWeight()));
            adjacencyList.computeIfAbsent(edge.getNodeTo(), k -> new ArrayList<>())
                    .add(new EdgeInfo(edge.getNodeFrom(), edge.getWeight()));
        }

        // Dijkstra 초기화
        Map<Long, BigDecimal> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
                Comparator.comparing(nd -> nd.distance)
        );

        for (VenueNode node : allNodes) {
            distances.put(node.getNodeId(), BigDecimal.valueOf(Double.MAX_VALUE));
        }
        distances.put(startNodeId, BigDecimal.ZERO);
        pq.offer(new NodeDistance(startNodeId, BigDecimal.ZERO));

        // Dijkstra 실행
        Set<Long> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Long currentNodeId = current.nodeId;

            if (visited.contains(currentNodeId)) continue;
            visited.add(currentNodeId);

            if (currentNodeId.equals(endNodeId)) break;

            List<EdgeInfo> neighbors = adjacencyList.getOrDefault(currentNodeId, Collections.emptyList());

            for (EdgeInfo edge : neighbors) {
                if (visited.contains(edge.toNodeId)) continue;

                BigDecimal newDistance = distances.get(currentNodeId).add(edge.weight);

                if (newDistance.compareTo(distances.get(edge.toNodeId)) < 0) {
                    distances.put(edge.toNodeId, newDistance);
                    previous.put(edge.toNodeId, currentNodeId);
                    pq.offer(new NodeDistance(edge.toNodeId, newDistance));
                }
            }
        }

        // 경로 재구성
        List<VenueNode> path = new ArrayList<>();
        Long currentNodeId = endNodeId;

        while (currentNodeId != null) {
            path.add(0, nodeMap.get(currentNodeId));
            currentNodeId = previous.get(currentNodeId);
        }

        return path;
    }

    /**
     * 두 좌표 사이의 유클리드 거리 계산
     */
    private BigDecimal calculateDistance(BigDecimal x1, BigDecimal y1,
                                         BigDecimal x2, BigDecimal y2) {
        double dx = x1.doubleValue() - x2.doubleValue();
        double dy = y1.doubleValue() - y2.doubleValue();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 경로의 총 거리 계산
     */
    private BigDecimal calculatePathDistance(List<VenueNode> path) {
        if (path.size() < 2) return BigDecimal.ZERO;

        BigDecimal totalDistance = BigDecimal.ZERO;

        for (int i = 0; i < path.size() - 1; i++) {
            VenueNode current = path.get(i);
            VenueNode next = path.get(i + 1);
            totalDistance = totalDistance.add(
                    calculateDistance(current.getX(), current.getY(), next.getX(), next.getY())
            );
        }

        return totalDistance;
    }

    /**
     * 특정 구역 내의 점이 해당 구역 안에 있는지 확인 (Point-in-Polygon)
     */
    public boolean isPointInSection(Long venueId, String sectionId,
                                    BigDecimal x, BigDecimal y) {
        VenueSection section = sectionRepository.findByVenueIdAndSectionId(venueId, sectionId);
        if (section == null) return false;

        List<Coordinate> vertices = section.getVerticesList();
        return isPointInPolygon(x, y, vertices);
    }

    /**
     * Point-in-Polygon 알고리즘 (Ray Casting)
     */
    private boolean isPointInPolygon(BigDecimal x, BigDecimal y, List<Coordinate> vertices) {
        int intersections = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Coordinate v1 = vertices.get(i);
            Coordinate v2 = vertices.get((i + 1) % n);

            if (rayCrossesSegment(x, y, v1, v2)) {
                intersections++;
            }
        }

        return (intersections % 2) == 1;
    }

    /**
     * 수평 광선이 선분과 교차하는지 확인
     */
    private boolean rayCrossesSegment(BigDecimal px, BigDecimal py,
                                      Coordinate v1, Coordinate v2) {
        double x = px.doubleValue();
        double y = py.doubleValue();
        double x1 = v1.getX().doubleValue();
        double y1 = v1.getY().doubleValue();
        double x2 = v2.getX().doubleValue();
        double y2 = v2.getY().doubleValue();

        if (y1 > y2) {
            double temp = y1; y1 = y2; y2 = temp;
            temp = x1; x1 = x2; x2 = temp;
        }

        if (y < y1 || y >= y2) return false;
        if (x >= Math.max(x1, x2)) return false;

        if (x < Math.min(x1, x2)) return true;

        double xIntersection = (y - y1) * (x2 - x1) / (y2 - y1) + x1;
        return x < xIntersection;
    }

    /**
     * 구역 내 임의의 점에서 시설물까지의 경로 찾기
     */
    public PathFindingResponse findPathToFacility(Long venueId,
                                                  BigDecimal startX, BigDecimal startY,
                                                  Long facilityId) {
        // 시설물 노드는 일반 노드보다 큰 ID를 가지므로 직접 조회
        VenueNode facilityNode = nodeRepository.findById(facilityId).orElse(null);

        if (facilityNode != null) {
            return findPath(venueId, startX, startY, facilityNode.getX(), facilityNode.getY());
        }

        return PathFindingResponse.builder()
                .path(Collections.emptyList())
                .totalDistance(BigDecimal.ZERO)
                .nodeCount(0)
                .build();
    }

    // ============================================
    // 내부 헬퍼 클래스
    // ============================================

    @AllArgsConstructor
    private static class EdgeInfo {
        Long toNodeId;
        BigDecimal weight;
    }

    @AllArgsConstructor
    private static class NodeDistance {
        Long nodeId;
        BigDecimal distance;
    }
}
