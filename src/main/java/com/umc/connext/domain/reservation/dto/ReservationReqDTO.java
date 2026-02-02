package com.umc.connext.domain.reservation.dto;

public class ReservationReqDTO {

    public record ReservationAddReqDTO(
            Long concertDetailId,
            SeatInfoDTO seatInfo
    ){}

}
