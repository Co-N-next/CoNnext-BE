package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class ReservationResDTO {

    @Builder
    public record ReservationAddResDTO(
            Long reservationId,
            Long concertDetailId
    ){}

    @Builder
    public record ReservationGetResDTO(
            Long reservationId,
            String concertName,
            String artist,
            LocalDateTime startAt,
            String venueName,
            SeatInfoDTO seat
    ){}

    @Builder
    public record ReservationRawDTO(
            Long reservationId,
            String concertName,
            LocalDateTime startAt,
            String venueName,
            String artist,
            Integer floor,
            String section,
            String row,
            Integer number
    ){}

}
