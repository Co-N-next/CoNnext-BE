package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationGetResDTO(
        Long reservationId,
        String concertName,
        String artist,
        LocalDateTime startAt,
        String venueName,
        SeatInfoDTO seat
){}
