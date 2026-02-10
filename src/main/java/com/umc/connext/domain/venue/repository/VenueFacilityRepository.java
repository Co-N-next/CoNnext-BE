package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueFacilityRepository extends JpaRepository<VenueFacility, Long> {

    // 공연장의 모든 시설물 조회
    List<VenueFacility> findAllByVenueId(Long venueId);

    // 공연장의 특정 층 시설물 조회
    List<VenueFacility> findAllByVenueIdAndFloor(Long venueId, Integer floor);

    // 공연장의 특정 타입 시설물 조회
    List<VenueFacility> findAllByVenueIdAndType(Long venueId, String type);

    // 공연장의 특정 층의 특정 타입 시설물 조회
    List<VenueFacility> findAllByVenueIdAndFloorAndType(Long venueId, Integer floor, String type);

    // 공연장의 모든 계단 조회
    @Query("SELECT f FROM VenueFacility f WHERE f.venueId = :venueId AND f.type = 'STAIRS'")
    List<VenueFacility> findAllStairsByVenueId(@Param("venueId") Long venueId);

    // 특정 두 층을 연결하는 계단 조회 (connectedFloors에 두 층 번호가 모두 포함된 경우)
    @Query("SELECT f FROM VenueFacility f WHERE f.venueId = :venueId " +
            "AND f.type = 'STAIRS' " +
            "AND f.connectedFloors LIKE CONCAT('%', :floor1, '%') " +
            "AND f.connectedFloors LIKE CONCAT('%', :floor2, '%')")
    List<VenueFacility> findStairsConnectingFloors(
            @Param("venueId") Long venueId,
            @Param("floor1") String floor1,
            @Param("floor2") String floor2
    );

    // 특정 두 층을 연결하는 계단 조회 (Integer 파라미터 버전)
    default List<VenueFacility> findStairsConnectingFloors(
            Long venueId,
            Integer floor1,
            Integer floor2
    ) {
        return findStairsConnectingFloors(
                venueId,
                String.valueOf(floor1),
                String.valueOf(floor2)
        );
    }

    // ID와 공연장 ID로 시설물 단건 조회
    Optional<VenueFacility> findByIdAndVenueId(Long id, Long venueId);

    // 공연장의 전체 시설물 수 카운트
    long countByVenueId(Long venueId);

    // 공연장의 특정 층 시설물 수 카운트
    long countByVenueIdAndFloor(Long venueId, Integer floor);

    // 공연장의 모든 시설물 삭제
    void deleteAllByVenueId(Long venueId);
}
