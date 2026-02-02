package com.umc.connext.domain.venue.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.connext.common.enums.FacilityType;
import com.umc.connext.common.enums.SectionType;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueResponse;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.repository.VenueFacilityRepository;
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueSectionRepository venueSectionRepository;
    private final VenueFacilityRepository venueFacilityRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final VenueRepository venueRepository;

    // 공연장 검색
    @Transactional(readOnly = true)
    public Page<VenueResponse.VenuePreviewDTO> searchVenues(
            String query,
            Integer page
    ) {
        PageRequest pageRequest = PageRequest.of(page, 10);

        return venueRepository.searchVenues(query, pageRequest)
                .map(VenueConverter::toVenuePreviewDTO);
    }

    // 인기 검색 공연장 조회
    @Transactional
    public List<VenueResponse.VenuePreviewDTO> trendSearchVenues() {

        // searchCount가 가장 높은 것부터 10개 조회
        List<Venue> top5BySearchCount = venueRepository.findTop5ByOrderBySearchCountDesc();

        // DTO 변환
        return top5BySearchCount.stream()
                .map(VenueConverter::toVenuePreviewDTO)
                .toList();
    }

    public VenueResponse getVenueMap(Long venueId) {
        validateVenueId(venueId);

        // 공연장 기본 정보 조회
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

        venueRepository.incrementTotalViews(venueId);

        // 구역(Section) 및 시설(Facility) 조회
        List<VenueSection> sections = venueSectionRepository.findAllByVenueId(venueId);
        List<VenueFacility> facilities = venueFacilityRepository.findAllByVenueId(venueId);

        // 층별 데이터 그룹화
        Map<Integer, List<VenueSection>> sectionsByFloor = sections.stream()
                .collect(Collectors.groupingBy(VenueSection::getFloor));
        Map<Integer, List<VenueFacility>> facilitiesByFloor = facilities.stream()
                .collect(Collectors.groupingBy(VenueFacility::getFloor));

        // 존재하는 모든 층 수집
        Set<Integer> allFloors = new HashSet<>();
        allFloors.addAll(sectionsByFloor.keySet());
        allFloors.addAll(facilitiesByFloor.keySet());

        List<VenueResponse.FloorData> floorDataList = new ArrayList<>();

        // 층별 DTO 생성
        for (Integer floor : allFloors) {
            List<VenueResponse.SectionDto> sectionDtos = sectionsByFloor.getOrDefault(floor, Collections.emptyList())
                    .stream()
                    .map(this::convertToSectionDto)
                    .collect(Collectors.toList());

            List<VenueResponse.FacilityDto> facilityDtos = facilitiesByFloor.getOrDefault(floor, Collections.emptyList())
                    .stream()
                    .map(this::convertToFacilityDto)
                    .collect(Collectors.toList());

            floorDataList.add(VenueResponse.FloorData.builder()
                    .floor(floor)
                    .sections(sectionDtos)
                    .facilities(facilityDtos)
                    .build());
        }

        floorDataList.sort(Comparator.comparingInt(VenueResponse.FloorData::getFloor));

        return VenueResponse.builder()
                .venueId(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .totalFloors(venue.getTotalFloors())
                .totalViews(venue.getTotalViews())
                .svgWidth(venue.getSvgWidth())
                .svgHeight(venue.getSvgHeight())
                .floors(floorDataList)
                .build();
    }

    private VenueResponse.SectionDto convertToSectionDto(VenueSection section) {
        String finalPathData = section.getFullPath();

        if (finalPathData == null || finalPathData.isEmpty()) {
            finalPathData = convertVerticesToSvgPath(section.getVertices());
        }

        // SectionType null 방어 로직
        String typeStr = (section.getType() != null) ? section.getType().toString() : SectionType.UNKNOWN.toString();

        return VenueResponse.SectionDto.builder()
                .sectionId(section.getSectionId())
                .type(typeStr)
                .pathData(finalPathData)
                .build();
    }

    /**
     * VenueFacility를 FacilityDto로 변환
     */
    private VenueResponse.FacilityDto convertToFacilityDto(VenueFacility facility) {
        String typeStr = (facility.getType() != null) ? facility.getType() : FacilityType.ETC.toString();
        return VenueResponse.FacilityDto.builder()
                .facilityId(facility.getId())
                .type(typeStr)
                .name(facility.getName())
                .x(facility.getX())
                .y(facility.getY())
                .build();
    }

    private String convertVerticesToSvgPath(String verticesJson) {
        if (verticesJson == null || verticesJson.isEmpty()) return "";
        try {
            List<Map<String, Object>> points = objectMapper.readValue(verticesJson, new TypeReference<>() {});
            if (points.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            Map<String, Object> start = points.get(0);
            sb.append("M ").append(start.get("x")).append(" ").append(start.get("y"));

            for (int i = 1; i < points.size(); i++) {
                Map<String, Object> p = points.get(i);
                sb.append(" L ").append(p.get("x")).append(" ").append(p.get("y"));
            }
            sb.append(" Z");
            return sb.toString();
        } catch (Exception e) {
            log.warn("SVG 변환 오류: {}", e.getMessage());
            return "";
        }
    }

    public List<VenueResponse.FacilityDto> getVenueFacilities(Long venueId, Integer floor, String type) {
        validateVenueId(venueId);

        List<VenueFacility> facilities = venueFacilityRepository.findAllByVenueId(venueId);

        List<VenueResponse.FacilityDto> result = facilities.stream()
                .filter(f -> floor == null || f.getFloor().equals(floor))
                .filter(f -> type == null || f.getType().equalsIgnoreCase(type))
                .map(this::convertToFacilityDto)
                .collect(Collectors.toList());

        log.debug("공연장 {} 시설물 조회 완료 - 조건: floor={}, type={}, 결과: {} 개",
                venueId, floor, type, result.size());

        return result;
    }

    public List<VenueResponse.FacilityDto> getVenueFacilities(Long venueId) {
        // 1. 해당 공연장의 모든 시설물 조회
        List<VenueFacility> facilities = venueFacilityRepository.findAllByVenueId(venueId);

        // 2. Entity -> DTO 변환하여 반환
        return facilities.stream()
                .map(VenueResponse.FacilityDto::from) // DTO에 from 메서드가 있다고 가정 (없으면 builder 사용)
                .collect(Collectors.toList());
    }

    // ==================== Validation Methods ====================

    /**
     * VenueId 입력값 검증
     */
    private void validateVenueId(Long venueId) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }
    }
}