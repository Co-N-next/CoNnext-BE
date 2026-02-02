package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // 이름으로 공연장 조회
    Optional<Venue> findByName(String name);

    // 활성화된 공연장 목록 조회
    List<Venue> findAllByIsActiveTrue();

    // 이름으로 공연장 존재 여부 확인
    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Venue v SET v.totalViews = v.totalViews + 1 WHERE v.id = :venueId")
    int incrementTotalViews(@Param("venueId") Long venueId);
}
