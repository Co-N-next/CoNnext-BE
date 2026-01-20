package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.VenueSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueSectionRepository extends JpaRepository<VenueSection, Long> {

    @Query("SELECT vs FROM VenueSection vs WHERE vs.venueId = :venueId AND vs.sectionId = :sectionId")
    VenueSection findByVenueIdAndSectionId(@Param("venueId") Long venueId, @Param("sectionId") String sectionId);

    List<VenueSection> findAllByVenueId(Long venueId);
}