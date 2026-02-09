package com.umc.connext.domain.reservation.converter;

import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.dto.ReservationGetResDTO;
import com.umc.connext.domain.reservation.dto.ReservationReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.entity.Reservation;

public class ReservationConverter {

    // DTO -> Entity
    public static Reservation toReservation(
            ReservationReqDTO.ReservationAddReqDTO dto,
            Member member,
            ConcertDetail concertDetail
    ){
        return Reservation.builder()
                .member(member)
                .concertDetail(concertDetail)
                .floor(dto.seatInfo().floor())
                .section(dto.seatInfo().section())
                .row(dto.seatInfo().row())
                .seat(dto.seatInfo().seat())
                .build();

    }

    // Entity -> DTO
    public static ReservationResDTO.ReservationAddResDTO toReservationAddResDTO(
            Reservation reservation
    ){
        return ReservationResDTO.ReservationAddResDTO.builder()
                .reservationId(reservation.getId())
                .concertDetailId(reservation.getConcertDetail().getId())
                .build();
    }

}
