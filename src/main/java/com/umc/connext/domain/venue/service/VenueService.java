package com.umc.connext.domain.venue.service;

<<<<<<< feat/favorite-venues
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.venue.entity.FavoriteVenue;
import com.umc.connext.domain.venue.repository.FavoriteVenueRepository;
=======
import com.umc.connext.domain.venue.projection.SimpleVenue;
>>>>>>> dev
import com.umc.connext.domain.venue.repository.VenueRepository;
import com.umc.connext.domain.venue.converter.VenueConverter;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.entity.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    private final FavoriteVenueRepository favoriteVenueRepository;
    private final MemberRepository memberRepository;

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

<<<<<<< feat/favorite-venues
    @PersistenceContext
    private EntityManager em;

    // 공연장 즐겨찾기 등록
    @Transactional
    public VenueResDTO.VenueSimpleDTO addFavoriteVenue(
            Long memberId,
            Long venueId
    ){
        // 공연장 존재 확인
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다."));

        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        // 이미 즐겨찾기된 경우
        if (favoriteVenueRepository.existsByMemberIdAndVenueId(memberId, venueId)) {
            return VenueConverter.toVenueSimpleDTO(venue); // 이미 즐겨찾기여도 성공
        }

        // 즐겨찾기 생성
        FavoriteVenue favoriteVenue = VenueConverter.toFavoriteVenue(member, venue);
        // DB 적용
        favoriteVenueRepository.save(favoriteVenue);

        return VenueConverter.toVenueSimpleDTO(venue);

    }

    // 공연장 즐겨찾기 삭제
    @Transactional
    public void deleteFavoriteVenue(
            Long memberId,
            Long venueId
    ){
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        venueRepository.findById(venueId)
                .orElseThrow(() -> GeneralException.notFound("공연장을 찾을 수 없습니다."));

        favoriteVenueRepository.deleteByMemberIdAndVenueId(memberId, venueId);
    }

    // 공연장 즐겨찾기 목록 조회
    @Transactional(readOnly = true)
    public List<VenueResDTO.VenuePreviewDTO> favoriteVenues(
            Long memberId
    ){
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        List<FavoriteVenue> favorites = favoriteVenueRepository.findAllByMemberIdFetchVenue(memberId);

        return favorites.stream()
                .map(FavoriteVenue::getVenue)
                .map(VenueConverter::toVenuePreviewDTO)
                .toList();
=======
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

        minLat = Math.max(minLat, -90.0);
        maxLat = Math.min(maxLat, 90.0);
        minLng = Math.max(minLng, -180.0);
        maxLng = Math.min(maxLng, 180.0);

        // BoundingBox 내의 공연장 조회
        return venueRepository.findNearbyVenue(minLat, maxLat, minLng, maxLng, lat, lng, radius);
>>>>>>> dev
    }

}
