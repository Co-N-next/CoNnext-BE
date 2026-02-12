package com.umc.connext.domain.mate.dto;

import com.umc.connext.domain.reservation.dto.SeatInfoDTO;
import lombok.Builder;

@Builder
public record TodayMateResDTO(
        String concertName,
        String concertPosterImage,
        Long mateId,
        Long memberId,
        String nickname,
        String profileImage,
        SeatInfoDTO seatInfo

){}