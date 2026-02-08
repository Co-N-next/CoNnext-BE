package com.umc.connext.domain.venue.service;

import com.umc.connext.domain.venue.projection.SimpleVenue;
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.entity.Venue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    // 공연장 검색
    @Transactional(readOnly = true)
    public Page<VenueResDTO.VenuePreviewDTO> searchVenues(
            String query,
            Integer page
    ) {
        PageRequest pageRequest = PageRequest.of(page, 10);

        return venueRepository.searchVenues(query, pageRequest)
                .map(VenueConverter::toVenuePreviewDTO);
    }

    // 인기 검색 공연장 조회
    @Transactional(readOnly = true)
    public List<VenueResDTO.VenuePreviewDTO> trendSearchVenues() {

        // searchCount가 가장 높은 것부터 5개 조회
        List<Venue> top5BySearchCount = venueRepository.findTop5ByOrderBySearchCountDesc();

        // DTO 변환
        return top5BySearchCount.stream()
                .map(VenueConverter::toVenuePreviewDTO)
                .toList();
    }

    // 근처 공연장 조회
    @Transactional(readOnly = true)
    public Optional<SimpleVenue> nearbyVenue(
            Double lat,
            Double lng,
            int radius
    ) {
        // BoundingBox 먼저 계산
        double latDelta = radius / 111_320.0; // 위도
        double cosLat = Math.cos(Math.toRadians(lat));
        double lngDelta;

        if (Math.abs(cosLat) < 1e-6) {
            lngDelta = 180.0;
        } else {
            lngDelta = radius / (111_320.0 * cosLat); // 경도
            if (lngDelta > 180.0) lngDelta = 180.0;
        }

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        // BoundingBox 내의 공연장 조회
        return venueRepository.findNearbyVenue(minLat, maxLat, minLng, maxLng, lat, lng, radius);
    }

}
