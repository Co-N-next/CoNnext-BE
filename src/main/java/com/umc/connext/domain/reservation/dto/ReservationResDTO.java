package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

public class ReservationResDTO {

    @Builder
    public record ReservationAddResDTO(
            Long reservationId,
            Long concertDetailId
    ){}

    @Builder
    public record ReservationUpdateResDTO(
            Long reservationId,
            Long concertDetailId,
            SeatInfoDTO seat
    ){}

}
