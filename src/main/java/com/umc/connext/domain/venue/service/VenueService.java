package com.umc.connext.domain.venue.service;

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
}