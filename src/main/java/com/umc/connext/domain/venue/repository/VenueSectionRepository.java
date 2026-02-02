package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueSectionRepository extends JpaRepository<VenueSection, Long> {

    // 공연장의 모든 구역 조회
    List<VenueSection> findAllByVenueId(Long venueId);

    // 공연장의 특정 층 구역 조회
    List<VenueSection> findAllByVenueIdAndFloor(Long venueId, Integer floor);

    // 공연장의 특정 구역 조회
    Optional<VenueSection> findByVenueIdAndSectionId(Long venueId, String sectionId);

    // 구역 존재 여부 확인
    boolean existsByVenueIdAndSectionId(Long venueId, String sectionId);

    // 공연장의 구역 수 카운트
    long countByVenueId(Long venueId);

    // 공연장의 특정 층 구역 수 카운트
    long countByVenueIdAndFloor(Long venueId, Integer floor);
}
