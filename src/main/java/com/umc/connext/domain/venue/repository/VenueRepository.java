package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.projection.SearchVenue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query(value = """
        SELECT 
            v.id AS id,
            v.name AS name,
            v.city AS city,
            v.address AS address,
            v.image_url AS imageUrl
        FROM venues v
        WHERE
            v.name LIKE CONCAT('%', :q, '%')
            OR v.city LIKE CONCAT('%', :q, '%') 
            OR v.address LIKE CONCAT('%', :q, '%')
        ORDER BY
            CASE
                WHEN v.name LIKE CONCAT('%', :q, '%') THEN 1
                WHEN v.city LIKE CONCAT('%', :q, '%') THEN 2
                WHEN v.address LIKE CONCAT('%', :q, '%') THEN 3
                ELSE 4
            END,
            v.name ASC,
            v.id ASC
        """, nativeQuery = true)
    Page<SearchVenue> searchVenues(
            @Param("q") String q,
            PageRequest pageRequest
    );

    List<Venue> findTop5ByOrderBySearchCountDesc();
}