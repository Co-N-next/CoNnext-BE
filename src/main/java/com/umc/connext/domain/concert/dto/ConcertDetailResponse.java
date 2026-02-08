package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ConcertDetailResponse {

    // Concert 정보
    private Long concertId;
    private String name;
    private String posterImage;
    private String ageRating;
    private String noticeUrl;

    // ConcertDetail 정보
    private Long detailId;
    private LocalDateTime startAt;
    private Integer runningTime;
    private Integer intermission;
    private Integer round;

    public static ConcertDetailResponse from(ConcertDetail detail) {
        Concert concert = detail.getConcert();

        return ConcertDetailResponse.builder()
                .concertId(concert.getId())
                .name(concert.getName())
                .posterImage(concert.getPosterImage())
                .ageRating(concert.getAgeRating())
                .noticeUrl(concert.getNoticeUrl())
                .detailId(detail.getId())
                .startAt(detail.getStartAt())
                .runningTime(detail.getRunningTime())
                .intermission(detail.getIntermission())
                .round(detail.getRound())
                .build();
    }
}