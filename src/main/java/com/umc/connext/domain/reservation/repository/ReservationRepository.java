package com.umc.connext.domain.reservation.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

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

    List<Reservation> findAllByMember(Member member);
}
