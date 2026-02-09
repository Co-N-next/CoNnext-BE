package com.umc.connext.domain.reservation.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.dto.ReservationGetResDTO;
import com.umc.connext.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByMemberIdAndConcertDetailIdAndFloorAndSectionAndRowAndSeat(
            Long memberId,
            Long aLong,
            Integer floor,
            String section,
            String row,
            Integer seat
    );

    void deleteByIdAndMemberId(Long reservationId, Long memberId);

    @Query("""
        SELECT new com.umc.connext.domain.reservation.dto.ReservationGetResDTO(
            r.id,
            c.name,
            cast.name,
            cd.startAt,
            v.name,
            new com.umc.connext.domain.reservation.dto.SeatInfoDTO(
                r.floor,
                r.section,
                r.row,
                r.seat
            )
        ) FROM Reservation r
        JOIN r.concertDetail cd
        JOIN cd.concert c
        JOIN c.concertCasts cc
        JOIN cc.cast cast
        JOIN c.concertVenues cv
        JOIN cv.venue v
        WHERE r.member = :member
        ORDER BY r.id DESC
    """)
    List<ReservationGetResDTO> findAllByMember(Member member);

    Optional<Reservation> findFirstByMemberIdAndConcertDetail_StartAtBetweenOrderByConcertDetail_StartAtAsc(Long memberId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
