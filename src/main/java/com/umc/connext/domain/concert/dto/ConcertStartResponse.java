package com.umc.connext.domain.concert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "공연 응답")
public record ConcertStartResponse(
        Long concertId,
        LocalDateTime startAt
) {}