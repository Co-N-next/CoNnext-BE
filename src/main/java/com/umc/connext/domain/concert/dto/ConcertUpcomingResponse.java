package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.ConcertDetail;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class ConcertUpcomingResponse {
    private Long concertId;
    private Long detailId;
    private String concertName;
    private String posterImage;
    private LocalDateTime startAt;
    private String dDay;

    public static ConcertUpcomingResponse from(ConcertDetail detail) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), detail.getStartAt().toLocalDate());
        String dDay = days == 0 ? "D-Day" : "D-" + days;

        return ConcertUpcomingResponse.builder()
                .concertId(detail.getConcert().getId())
                .detailId(detail.getId())
                .concertName(detail.getConcert().getName())
                .posterImage(detail.getConcert().getPosterImage())
                .startAt(detail.getStartAt())
                .dDay(dDay)
                .build();
    }
}