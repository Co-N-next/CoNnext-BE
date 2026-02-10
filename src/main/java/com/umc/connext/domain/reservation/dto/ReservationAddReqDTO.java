package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

@Builder
public record ReservationAddReqDTO(
        Long concertDetailId,
        SeatInfoDTO seatInfo
){}