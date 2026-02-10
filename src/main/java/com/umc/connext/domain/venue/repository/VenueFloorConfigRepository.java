package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueFloorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VenueFloorConfigRepository extends JpaRepository<VenueFloorConfig, Long> {

    // 공연장의 모든 층 설정 조회
    List<VenueFloorConfig> findAllByVenueId(Long venueId);

    // 공연장의 특정 층 섹션 목록 조회
    List<VenueFloorConfig> findAllByVenueIdAndFloor(Long venueId, Integer floor);

    // 특정 섹션의 층 설정 조회
    Optional<VenueFloorConfig> findByVenueIdAndSectionId(Long venueId, String sectionId);

    // 공연장의 특정 층 섹션 ID 목록 조회
    @Query("SELECT c.sectionId FROM VenueFloorConfig c WHERE c.venueId = :venueId AND c.floor = :floor")
    Set<String> findSectionIdsByVenueIdAndFloor(
            @Param("venueId") Long venueId,
            @Param("floor") Integer floor
    );

    // 공연장에 존재하는 모든 층 번호 조회
    @Query("SELECT DISTINCT c.floor FROM VenueFloorConfig c WHERE c.venueId = :venueId ORDER BY c.floor")
    List<Integer> findDistinctFloorsByVenueId(@Param("venueId") Long venueId);

    // 공연장의 층 설정 존재 여부
    boolean existsByVenueId(Long venueId);

    // 공연장의 모든 층 설정 삭제
    @Modifying
    @Query("DELETE FROM VenueFloorConfig c WHERE c.venueId = :venueId")
    void deleteAllByVenueId(@Param("venueId") Long venueId);

    // 특정 섹션 설정 삭제
    @Modifying
    void deleteByVenueIdAndSectionId(Long venueId, String sectionId);
}
