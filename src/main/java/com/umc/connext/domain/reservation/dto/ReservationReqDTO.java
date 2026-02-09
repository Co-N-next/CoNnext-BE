package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

@Builder
public record ReservationReqDTO(
        Long concertDetailId,
        SeatInfoDTO seatInfo
){}