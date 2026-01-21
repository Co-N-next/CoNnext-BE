package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.ConcertDetail;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder

public class ConcertScheduleResponse {
    private Long detailId;
    private LocalDateTime startAt;
    private Integer round;
    private Integer runningTime;

    public static ConcertScheduleResponse from(ConcertDetail detail) {
        return ConcertScheduleResponse.builder()
                .detailId(detail.getId())
                .startAt(detail.getStartAt())
                .round(detail.getRound())
                .runningTime(detail.getRunningTime())
                .build();
    }
}
