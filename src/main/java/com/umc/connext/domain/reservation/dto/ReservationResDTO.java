package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

public class ReservationResDTO {

    @Builder
    public record ReservationAddResDTO(
            Long reservationId,
            Long concertDetailId
    ){}

}
