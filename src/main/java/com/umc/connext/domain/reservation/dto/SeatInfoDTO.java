package com.umc.connext.domain.reservation.dto;

public record SeatInfoDTO(
    Integer floor,
    String section,
    String row,
    Integer seat
){}
