package com.umc.connext.domain.venue.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.venue.entity.FavoriteVenue;
import com.umc.connext.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteVenueRepository extends JpaRepository<FavoriteVenue, Long> {

    boolean existsByMemberIdAndVenueId(Long memberId, Long venueId);

    void deleteByMemberIdAndVenueId(Long memberId, Long venueId);

    @Query("""
        SELECT fv
        FROM FavoriteVenue fv
        JOIN FETCH fv.venue
        WHERE fv.member.id = :memberId
    """)
    List<FavoriteVenue> findAllByMemberIdFetchVenue(@Param("memberId") Long memberId);

    boolean existsByMemberAndVenue(Member member, Venue venue);
}
