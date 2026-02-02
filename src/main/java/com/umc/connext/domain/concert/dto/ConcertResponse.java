package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "공연 응답")
public class ConcertResponse {

    @Schema(description = "공연 ID", example = "1")
    private Long id;

    @Schema(description = "공연명", example = "인디밴드 페스티벌")
    private String name;

    @Schema(description = "포스터 이미지 URL", example = "https://example.com/poster.jpg")
    private String posterImage;

    @Schema(description = "관람등급", example = "전체관람가")
    private String ageRating;

    @Schema(description = "공연 설명 주소", example = "https://example.com/notice")
    private String noticeUrl;

    @Schema(description = "가격", example = "50,000원")
    private String price;

    @Schema(description = "예매 링크", example = "https://ticket.example.com")
    private String reservationLink;

    @Schema(description = "공연 일정 목록")
    private List<ConcertScheduleResponse> schedules;

    // 상세 조회용
    public static ConcertResponse of(Concert concert, List<ConcertDetail> details) {
        return ConcertResponse.builder()
                .id(concert.getId())
                .name(concert.getName())
                .posterImage(concert.getPosterImage())
                .ageRating(concert.getAgeRating())
                .noticeUrl(concert.getNoticeUrl())
                .price(concert.getPrice())
                .reservationLink(concert.getReservationLink())
                .schedules(details.stream()
                        .map(ConcertScheduleResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }

    // 목록 조회용
    public static ConcertResponse from(Concert concert) {
        return ConcertResponse.builder()
                .id(concert.getId())
                .name(concert.getName())
                .posterImage(concert.getPosterImage())
                .ageRating(concert.getAgeRating())
                .noticeUrl(concert.getNoticeUrl())
                .price(concert.getPrice())
                .reservationLink(concert.getReservationLink())
                .build();
    }
}