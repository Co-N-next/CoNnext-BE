package com.umc.connext.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReservationAddReqDTO(
        @NotNull Long concertDetailId,
        @NotNull SeatInfoDTO seatInfo
){}