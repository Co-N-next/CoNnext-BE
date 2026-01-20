package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VenueFacilityRepository extends JpaRepository<VenueFacility, Long> {
    List<VenueFacility> findByVenueId(Long venueId);
    List<VenueFacility> findByVenueIdAndType(Long venueId, String type);

    @Query("SELECT vf FROM VenueFacility vf WHERE vf.venueId = :venueId " +
            "ORDER BY SQRT(POWER(vf.x - :x, 2) + POWER(vf.y - :y, 2)) ASC")
    List<VenueFacility> findNearestFacilities(@Param("venueId") Long venueId,
                                              @Param("x") BigDecimal x,
                                              @Param("y") BigDecimal y);
}