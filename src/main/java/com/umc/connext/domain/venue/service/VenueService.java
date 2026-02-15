package com.umc.connext.domain.venue.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.enums.FacilityType;
import com.umc.connext.common.enums.SectionType;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.reservation.entity.Reservation;
import com.umc.connext.domain.reservation.repository.ReservationRepository;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueLayoutResponse;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.entity.FavoriteVenue;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import com.umc.connext.domain.venue.projection.SimpleVenue;
import com.umc.connext.domain.venue.repository.FavoriteVenueRepository;
import com.umc.connext.domain.venue.repository.VenueFacilityRepository;
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final VenueFacilityRepository venueFacilityRepository;
    private final FavoriteVenueRepository favoriteVenueRepository;
    private final MemberRepository memberRepository;
    private final ConcertDetailRepository concertDetailRepository;
    private final ReservationRepository reservationRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final FloorMappingService floorMappingService;

    // ── 검색 ──

    public Page<VenueResDTO.VenuePreviewDTO> searchVenues(String query, Integer page) {
        PageRequest pageRequest = PageRequest.of(page, 10);
        return venueRepository.searchVenues(query, pageRequest)
                .map(VenueConverter::toVenuePreviewDTO);
    }

    public List<VenueResDTO.VenuePreviewDTO> trendSearchVenues() {
        List<Venue> top5BySearchCount = venueRepository.findTop5ByOrderBySearchCountDesc();
        return top5BySearchCount.stream()
                .map(VenueConverter::toVenuePreviewDTO)
                .toList();
    }

    public List<VenueResDTO.VenueHomeDTO> getPopularVenues() {
        List<Venue> top8Venues = venueRepository.findTop8WithConcertsByViewCount();
        LocalDate today = LocalDate.now();

        Map<Long, List<ConcertDetail>> detailsByConcertId = loadConcertDetails(top8Venues);

        return top8Venues.stream()
                .map(venue -> {
                    boolean isToday = venue.getConcertVenues().stream()
                            .map(cv -> detailsByConcertId.getOrDefault(cv.getConcert().getId(), List.of()))
                            .flatMap(Collection::stream)
                            .anyMatch(detail -> detail.getStartAt().toLocalDate().equals(today));

                    boolean isNew = venue.getCreatedAt().toLocalDate().equals(today);

                    return VenueConverter.toVenueHomeDTO(venue, isToday, isNew);
                })
                .toList();
    }

    private Map<Long, List<ConcertDetail>> loadConcertDetails(List<Venue> venues) {
        Set<Long> concertIds = venues.stream()
                .flatMap(venue -> venue.getConcertVenues().stream())
                .map(cv -> cv.getConcert().getId())
                .collect(Collectors.toSet());

        if (concertIds.isEmpty()) {
            return Map.of();
        }

        return concertDetailRepository.findByConcertIdIn(concertIds).stream()
                .collect(Collectors.groupingBy(detail -> detail.getConcert().getId()));
    }
    // ── 맵 & 길찾기 ──

    @Transactional
    public VenueResDTO.VenueMapResponse getVenueMap(Long venueId) {
        validateVenueId(venueId);

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

        venueRepository.incrementTotalViews(venueId);

        List<VenueSection> sections = venueSectionRepository.findAllByVenueId(venueId);
        List<VenueFacility> facilities = venueFacilityRepository.findAllByVenueId(venueId);

        Map<Integer, List<VenueSection>> sectionsByFloor = sections.stream()
                .collect(Collectors.groupingBy(VenueSection::getFloor));
        Map<Integer, List<VenueFacility>> facilitiesByFloor = facilities.stream()
                .collect(Collectors.groupingBy(VenueFacility::getFloor));

        Set<Integer> allFloors = new HashSet<>();
        allFloors.addAll(sectionsByFloor.keySet());
        allFloors.addAll(facilitiesByFloor.keySet());

        List<VenueResDTO.FloorData> floorDataList = new ArrayList<>();

        for (Integer floor : allFloors) {
            List<VenueResDTO.SectionDto> sectionDtos = sectionsByFloor.getOrDefault(floor, Collections.emptyList())
                    .stream()
                    .map(this::convertToSectionDto)
                    .collect(Collectors.toList());

            List<VenueResDTO.FacilityDto> facilityDtos = facilitiesByFloor.getOrDefault(floor, Collections.emptyList())
                    .stream()
                    .map(this::convertToFacilityDto)
                    .collect(Collectors.toList());

            floorDataList.add(VenueResDTO.FloorData.builder()
                    .floor(floor)
                    .sections(sectionDtos)
                    .facilities(facilityDtos)
                    .build());
        }

        floorDataList.sort(Comparator.comparingInt(VenueResDTO.FloorData::getFloor));

        return VenueResDTO.VenueMapResponse.builder()
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

    public List<VenueResDTO.FacilityDto> getVenueFacilities(Long venueId) {
        validateVenueId(venueId);

        List<VenueFacility> facilities = venueFacilityRepository.findAllByVenueId(venueId);

        return facilities.stream()
                .map(VenueResDTO.FacilityDto::from)
                .collect(Collectors.toList());
    }

    // ── 즐겨찾기 ──

    @Transactional
    public VenueResDTO.VenueSimpleDTO addFavoriteVenue(Long memberId, Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        if (favoriteVenueRepository.existsByMemberIdAndVenueId(memberId, venueId)) {
            return VenueConverter.toVenueSimpleDTO(venue);
        }

        FavoriteVenue favoriteVenue = VenueConverter.toFavoriteVenue(member, venue);
        favoriteVenueRepository.save(favoriteVenue);

        return VenueConverter.toVenueSimpleDTO(venue);
    }

    @Transactional
    public void deleteFavoriteVenue(Long memberId, Long venueId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다."));

        favoriteVenueRepository.deleteByMemberIdAndVenueId(memberId, venueId);
    }

    public List<VenueResDTO.VenuePreviewDTO> favoriteVenues(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        List<FavoriteVenue> favorites = favoriteVenueRepository.findAllByMemberIdFetchVenue(memberId);

        return favorites.stream()
                .map(FavoriteVenue::getVenue)
                .map(VenueConverter::toVenuePreviewDTO)
                .toList();
    }

    // 오늘의 공연장
    public Optional<VenueResDTO.VenuePreviewDTO> todayVenue(
            Long memberId
    ) {
        // 오늘의 공연 조회
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Reservation todayConcert = reservationRepository
                .findFirstByMemberIdAndConcertDetail_StartAtBetweenOrderByConcertDetail_StartAtAsc(
                        memberId, startOfDay, endOfDay
                ).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "오늘 예정된 공연이 없습니다."));

        Venue todayVenue = todayConcert
                .getConcertDetail()
                .getConcert()
                .getConcertVenues()
                .stream()
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "오늘 공연의 공연장을 찾을 수 없습니다."))
                .getVenue();

        return Optional.ofNullable(VenueConverter.toVenuePreviewDTO(todayVenue));
    }

    // ── 근처 공연장 ──

    public Optional<SimpleVenue> nearbyVenue(Double lat, Double lng, int radius) {
        double latDelta = radius / 111_320.0;
        double cosLat = Math.cos(Math.toRadians(lat));
        double lngDelta;

        if (Math.abs(cosLat) < 1e-6) {
            lngDelta = 180.0;
        } else {
            lngDelta = radius / (111_320.0 * cosLat);
            if (lngDelta > 180.0) lngDelta = 180.0;
        }

        double minLat = Math.max(lat - latDelta, -90.0);
        double maxLat = Math.min(lat + latDelta, 90.0);
        double minLng = Math.max(lng - lngDelta, -180.0);
        double maxLng = Math.min(lng + lngDelta, 180.0);

        return venueRepository.findNearbyVenue(minLat, maxLat, minLng, maxLng, lat, lng, radius);
    }

    public VenueLayoutResponse getVenueLayout(Long venueId, Integer floor) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

        // 섹션 조회
        List<VenueSection> sections = (floor != null)
                ? venueSectionRepository.findAllByVenueIdAndFloor(venueId, floor)
                : venueSectionRepository.findAllByVenueId(venueId);

        // 시설물 조회
        List<VenueFacility> facilities = (floor != null)
                ? venueFacilityRepository.findAllByVenueIdAndFloor(venueId, floor)
                : venueFacilityRepository.findAllByVenueId(venueId);

        // 사용 가능한 층 목록
        List<Integer> floors = floorMappingService.getFloors(venueId);

        return VenueLayoutResponse.builder()
                .venue(mapToVenueInfo(venue))
                .sections(mapToSectionInfoList(sections))
                .facilities(mapToFacilityInfoList(facilities))
                .floors(floors)
                .build();
    }

    public VenueLayoutResponse.VenueInfo getVenueInfo(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다. ID=" + venueId));

        return mapToVenueInfo(venue);
    }

    // ===== 매핑 메서드 =====

    private VenueLayoutResponse.VenueInfo mapToVenueInfo(Venue venue) {
        return VenueLayoutResponse.VenueInfo.builder()
                .venueId(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .svgWidth(venue.getSvgWidth())
                .svgHeight(venue.getSvgHeight())
                .totalFloors(venue.getTotalFloors())
                .build();
    }

    private List<VenueLayoutResponse.SectionInfo> mapToSectionInfoList(List<VenueSection> sections) {
        return sections.stream()
                .map(this::mapToSectionInfo)
                .collect(Collectors.toList());
    }

    private VenueLayoutResponse.SectionInfo mapToSectionInfo(VenueSection section) {
        return VenueLayoutResponse.SectionInfo.builder()
                .sectionId(section.getSectionId())
                .floor(section.getFloor())
                .svgPath(section.getFullPath())
                .centerX(section.getCenterX())
                .centerY(section.getCenterY())
                .type(section.getType())
                .vertices(section.getVerticesList())
                .build();
    }

    private List<VenueLayoutResponse.FacilityInfo> mapToFacilityInfoList(List<VenueFacility> facilities) {
        return facilities.stream()
                .map(this::mapToFacilityInfo)
                .collect(Collectors.toList());
    }

    private VenueLayoutResponse.FacilityInfo mapToFacilityInfo(VenueFacility facility) {
        return VenueLayoutResponse.FacilityInfo.builder()
                .id(facility.getId())
                .name(facility.getName())
                .type(facility.getType())
                .floor(facility.getFloor())
                .x(facility.getX())
                .y(facility.getY())
                .connectedFloors(facility.getConnectedFloors())
                .build();
    }

    private VenueResDTO.SectionDto convertToSectionDto(VenueSection section) {
        String finalPathData = section.getFullPath();

        if (finalPathData == null || finalPathData.isEmpty()) {
            finalPathData = convertVerticesToSvgPath(section.getVertices());
        }

        String typeStr = (section.getType() != null) ? section.getType().toString() : SectionType.UNKNOWN.toString();

        return VenueResDTO.SectionDto.builder()
                .sectionId(section.getSectionId())
                .type(typeStr)
                .pathData(finalPathData)
                .build();
    }

    private VenueResDTO.FacilityDto convertToFacilityDto(VenueFacility facility) {
        String typeStr = (facility.getType() != null) ? facility.getType() : FacilityType.ETC.toString();
        return VenueResDTO.FacilityDto.builder()
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

    private void validateVenueId(Long venueId) {
        if (venueId == null || venueId <= 0) {
            throw GeneralException.notFound("유효하지 않은 공연장 ID입니다.");
        }
    }
}