package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.venue.entity.FavoriteVenue;
import com.umc.connext.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteVenueRepository extends JpaRepository<FavoriteVenue, Long> {

    boolean existsByMemberAndVenue(Member member, Venue venue);

    void deleteByMemberIdAndVenueId(Long memberId, Long venueId);
}
