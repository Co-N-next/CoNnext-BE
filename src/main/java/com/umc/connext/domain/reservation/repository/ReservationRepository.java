package com.umc.connext.domain.reservation.repository;

import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        SELECT new com.umc.connext.domain.reservation.dto.ReservationResDTO.ReservationRawDTO(
            r.id,
            c.name,
            cd.startAt,
            v.name,
            cast.name
            s.floor,
            s.section,
            s.row,
            s.number
        )
        FROM Reservation r
        JOIN r.concertDetail cd
        JOIN cd.concert c
        JOIN cd.
                    
    """)
}
