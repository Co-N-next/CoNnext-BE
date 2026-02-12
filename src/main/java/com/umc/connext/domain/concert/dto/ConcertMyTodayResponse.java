package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.ConcertDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 오늘의 공연 응답")
public class ConcertMyTodayResponse {

    @Schema(description = "예매 ID", example = "1")
    private Long reservationId;

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

    @Schema(description = "아티스트명", example = "싸이")
    private String artist;

    @Schema(description = "장소(공연장명)", example = "올림픽체조경기장")
    private String venue;

    @Schema(description = "층", example = "1")
    private Integer floor;

    @Schema(description = "구간", example = "A")
    private String section;

    @Schema(description = "행", example = "10")
    private String row;

    @Schema(description = "좌석", example = "5")
    private Integer seat;

    public static ConcertMyTodayResponse from(ConcertDetail detail, String artist, String venue, Integer floor, String section, String row, Integer seat, Long reservationId) {
        return ConcertMyTodayResponse.builder()
                .reservationId(reservationId)
                .concertId(detail.getConcert().getId())
                .concertName(detail.getConcert().getName())
                .posterImage(detail.getConcert().getPosterImage())
                .startAt(detail.getStartAt())
                .round(detail.getRound())
                .runningTime(detail.getRunningTime())
                .price(detail.getPrice())
                .artist(artist)
                .venue(venue)
                .floor(floor)
                .section(section)
                .row(row)
                .seat(seat)
                .build();
    }
}
