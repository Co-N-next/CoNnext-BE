package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findTop5ByOrderBySearchCountDesc();

}
