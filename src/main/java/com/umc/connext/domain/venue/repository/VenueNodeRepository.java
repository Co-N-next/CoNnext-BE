package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VenueNodeRepository extends JpaRepository<VenueNode, Long> {
    List<VenueNode> findByVenueId(Long venueId);

    @Query("SELECT vn FROM VenueNode vn WHERE vn.venueId = :venueId " +
            "ORDER BY SQRT(POWER(vn.x - :x, 2) + POWER(vn.y - :y, 2)) ASC")
    List<VenueNode> findNearestNodes(@Param("venueId") Long venueId,
                                     @Param("x") BigDecimal x,
                                     @Param("y") BigDecimal y);
}