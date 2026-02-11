package com.umc.connext.domain.reservation.dto;

import lombok.Builder;

@Builder
public record SeatInfoDTO(
    Integer floor,
    String section,
    String row,
    Integer seat
){}
