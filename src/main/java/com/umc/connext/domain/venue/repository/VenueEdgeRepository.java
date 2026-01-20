package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueEdgeRepository extends JpaRepository<VenueEdge, Long> {
    List<VenueEdge> findByVenueId(Long venueId);

    @Query("SELECT ve FROM VenueEdge ve WHERE ve.venueId = :venueId " +
            "AND (ve.nodeFrom = :nodeId OR ve.nodeTo = :nodeId)")
    List<VenueEdge> findEdgesByNodeId(@Param("venueId") Long venueId,
                                      @Param("nodeId") Long nodeId);
}