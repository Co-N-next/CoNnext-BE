package com.umc.connext.domain.venue.controller;

import com.umc.connext.domain.venue.dto.PathFindingResponse;
import com.umc.connext.domain.venue.service.PathFindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/venues/{venueId}/pathfinding")
@RequiredArgsConstructor
public class PathFindingController {

    private final PathFindingService pathFindingService;

    /**
     * 임의의 두 좌표 사이 경로 찾기
     */
    @GetMapping("/path")
    public PathFindingResponse findPath(
            @PathVariable Long venueId,
            @RequestParam BigDecimal startX,
            @RequestParam BigDecimal startY,
            @RequestParam BigDecimal endX,
            @RequestParam BigDecimal endY
    ) {
        return pathFindingService.findPath(venueId, startX, startY, endX, endY);
    }

    /**
     * 특정 좌표가 구역 내에 있는지 확인
     */
    @GetMapping("/sections/{sectionId}/contains")
    public boolean isPointInSection(
            @PathVariable Long venueId,
            @PathVariable String sectionId,
            @RequestParam BigDecimal x,
            @RequestParam BigDecimal y
    ) {
        return pathFindingService.isPointInSection(venueId, sectionId, x, y);
    }

    /**
     * 특정 좌표에서 시설물까지 경로 찾기
     */
    @GetMapping("/to-facility/{facilityId}")
    public PathFindingResponse findPathToFacility(
            @PathVariable Long venueId,
            @PathVariable Long facilityId,
            @RequestParam BigDecimal startX,
            @RequestParam BigDecimal startY
    ) {
        return pathFindingService.findPathToFacility(venueId, startX, startY, facilityId);
    }
}