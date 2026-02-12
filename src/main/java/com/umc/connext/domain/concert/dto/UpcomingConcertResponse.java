package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.Concert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "다가오는 공연 응답")
public class UpcomingConcertResponse {

    @Schema(description = "공연 ID", example = "1")
    private Long concertId;

    @Schema(description = "공연명", example = "인디밴드 페스티벌")
    private String concertName;

    @Schema(description = "포스터 이미지 URL", example = "https://example.com/poster.jpg")
    private String posterImage;

    @Schema(description = "관람등급", example = "전체관람가")
    private String ageRating;

    @Schema(description = "가장 가까운 공연 시작 시간", example = "2024-01-15T19:00:00")
    private LocalDateTime nextShowTime;

    @Schema(description = "가격", example = "50,000원")
    private String price;

    @Schema(description = "예매 링크", example = "https://ticket.example.com")
    private String reservationLink;

    @Schema(description = "조회수", example = "1500")
    private Long viewCount;

    public static UpcomingConcertResponse of(Concert concert, LocalDateTime nextShowTime, Long viewCount) {
        return UpcomingConcertResponse.builder()
                .concertId(concert.getId())
                .concertName(concert.getName())
                .posterImage(concert.getPosterImage())
                .ageRating(concert.getAgeRating())
                .nextShowTime(nextShowTime)
                .price(concert.getPrice())
                .reservationLink(concert.getReservationLink())
                .viewCount(concert.getViewCount())
                .build();
    }
}
