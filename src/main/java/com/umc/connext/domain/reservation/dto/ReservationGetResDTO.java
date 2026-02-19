package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationGetResDTO(
        Long reservationId,
        String concertName,
        String concertPosterImage,
        String artist,
        String artistImage,
        LocalDateTime startAt,
        String venueName,
        String venueImage,
        SeatInfoDTO seat
){}
