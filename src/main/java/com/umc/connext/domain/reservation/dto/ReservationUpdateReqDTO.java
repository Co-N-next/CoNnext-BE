package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

@Builder
public record ReservationUpdateReqDTO(
        Long concertDetailId,
        SeatInfoDTO seatInfo
) {}