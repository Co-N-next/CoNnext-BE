package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.entity.VenueFloorConfig;
import com.umc.connext.domain.venue.repository.VenueFloorConfigRepository;
import com.umc.connext.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorMappingService {

    private final VenueFloorConfigRepository floorConfigRepository;

    // 섹션 ID로 층 번호 조회 (캐시 적용)
    @Cacheable(value = "floorMapping", key = "#venueId + ':' + #sectionId")
    public int getFloor(Long venueId, String sectionId) {
        return floorConfigRepository.findByVenueIdAndSectionId(venueId, sectionId)
                .map(VenueFloorConfig::getFloor)
                .orElse(1);
    }

    // 특정 층의 모든 섹션 ID 조회 (캐시 적용)
    @Cacheable(value = "floorSections", key = "#venueId + ':' + #floor")
    public Set<String> getSectionIdsByFloor(Long venueId, Integer floor) {
        return floorConfigRepository.findSectionIdsByVenueIdAndFloor(venueId, floor);
    }

    // 공연장의 모든 층 목록 조회
    public List<Integer> getFloors(Long venueId) {
        return floorConfigRepository.findDistinctFloorsByVenueId(venueId);
    }

    // 공연장의 전체 층 매핑 조회
    public Map<String, Integer> getAllMappings(Long venueId) {
        List<VenueFloorConfig> configs = floorConfigRepository.findAllByVenueId(venueId);
        if (configs == null || configs.isEmpty()) {
            log.debug("VenueId {}에 대한 층 설정이 없습니다.", venueId);
            return new HashMap<>();
        }

        Map<String, Integer> mappings = new HashMap<>();
        for (VenueFloorConfig config : configs) {
            mappings.put(config.getSectionId(), config.getFloor());
        }
        return mappings;
    }

    // 층 설정 추가/업데이트 (캐시 전체 제거)
    @Transactional
    @CacheEvict(value = {"floorMapping", "floorSections"}, allEntries = true)
    public VenueFloorConfig setFloor(Long venueId, String sectionId, Integer floor, String description) {
        validateFloorInput(venueId, sectionId, floor);

        VenueFloorConfig config = floorConfigRepository.findByVenueIdAndSectionId(venueId, sectionId)
                .orElse(VenueFloorConfig.builder()
                        .venueId(venueId)
                        .sectionId(sectionId)
                        .build());

        config.setFloor(floor);
        config.setDescription(description);

        return floorConfigRepository.save(config);
    }

    // 여러 섹션 층 설정 일괄 추가/업데이트 (캐시 전체 제거)
    @Transactional
    @CacheEvict(value = {"floorMapping", "floorSections"}, allEntries = true)
    public List<VenueFloorConfig> setFloorsBatch(Long venueId, Map<String, Integer> sectionFloorMap) {
        if (sectionFloorMap == null || sectionFloorMap.isEmpty()) {
            throw GeneralException.notFound("섹션 층 매핑이 비어있습니다.");
        }

        List<VenueFloorConfig> configs = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sectionFloorMap.entrySet()) {
            validateFloorInput(venueId, entry.getKey(), entry.getValue());

            VenueFloorConfig config = floorConfigRepository
                    .findByVenueIdAndSectionId(venueId, entry.getKey())
                    .orElse(VenueFloorConfig.builder()
                            .venueId(venueId)
                            .sectionId(entry.getKey())
                            .build());

            config.setFloor(entry.getValue());
            configs.add(config);
        }

        return floorConfigRepository.saveAll(configs);
    }

    // 층 설정 삭제 (캐시 전체 제거)
    @Transactional
    @CacheEvict(value = {"floorMapping", "floorSections"}, allEntries = true)
    public void removeFloorConfig(Long venueId, String sectionId) {
        floorConfigRepository.deleteByVenueIdAndSectionId(venueId, sectionId);
    }

    // 공연장의 모든 층 설정 삭제 (캐시 전체 제거)
    @Transactional
    @CacheEvict(value = {"floorMapping", "floorSections"}, allEntries = true)
    public void removeAllFloorConfigs(Long venueId) {
        floorConfigRepository.deleteAllByVenueId(venueId);
    }

    // 공연장에 층 설정이 존재하는지 확인
    public boolean hasFloorConfig(Long venueId) {
        return floorConfigRepository.existsByVenueId(venueId);
    }

    // 좌표 기반 층 추정(설정 없을 때 fallback)
    public int estimateFloorByCoordinate(Long venueId, double x, double y, double svgWidth, double svgHeight) {
        double marginRatio = 0.15;

        boolean isLeftEdge = x < svgWidth * marginRatio;
        boolean isRightEdge = x > svgWidth * (1 - marginRatio);
        boolean isBottomEdge = y > svgHeight * (1 - marginRatio);

        if (isLeftEdge || isRightEdge || isBottomEdge) {
            return 2;
        }
        return 1;
    }

    // ==================== Private Methods ====================

    /**
     * 층 설정 입력값 검증
     */
    private void validateFloorInput(Long venueId, String sectionId, Integer floor) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }

        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw GeneralException.notFound("섹션 ID는 비어있을 수 없습니다.");
        }

        if (floor == null || floor <= 0) {
            throw GeneralException.notFound("층 번호는 1 이상이어야 합니다.");
        }

        if (floor > 100) {
            throw GeneralException.notFound("층 번호는 100 이하여야 합니다.");
        }
    }
}
