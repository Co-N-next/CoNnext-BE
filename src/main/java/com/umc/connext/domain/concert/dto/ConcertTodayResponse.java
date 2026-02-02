package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "오늘 공연 응답")
public class ConcertTodayResponse {

    @Schema(description = "공연 ID", example = "1")
    private Long concertId;

    @Schema(description = "공연명", example = "인디밴드 페스티벌")
    private String concertName;

    @Schema(description = "포스터 이미지 URL", example = "https://example.com/poster.jpg")
    private String posterImage;

    @Schema(description = "공연 시작 시간", example = "2024-01-15T19:00:00")
    private LocalDateTime startAt;

    @Schema(description = "회차", example = "1")
    private Integer round;

    @Schema(description = "러닝타임(분)", example = "120")
    private Integer runningTime;

    @Schema(description = "가격", example = "50000")
    private Integer price;

    public static ConcertTodayResponse from(ConcertDetail detail) {
        Concert concert = detail.getConcert();
        return ConcertTodayResponse.builder()
                .concertId(concert.getId())
                .concertName(concert.getName())
                .posterImage(concert.getPosterImage())
                .startAt(detail.getStartAt())
                .round(detail.getRound())
                .runningTime(detail.getRunningTime())
                .price(detail.getPrice())
                .build();
    }
}