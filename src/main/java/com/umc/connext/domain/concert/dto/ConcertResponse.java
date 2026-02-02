package com.umc.connext.domain.concert.dto;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ConcertResponse {

    private Long id;
    private String name;
    private String posterImage;
    private String ageRating;
    private String description;
    private String price;
    private String reservationLink;

    private List<ConcertScheduleResponse> schedules;

    public static ConcertResponse of(Concert concert, List<ConcertDetail> details) {
        return ConcertResponse.builder()
                .id(concert.getId())
                .name(concert.getName())
                .posterImage(concert.getPosterImage())
                .ageRating(concert.getAgeRating())
                .description(concert.getDescription())
                .price(concert.getPrice())
                .reservationLink(concert.getReservationLink())
                .schedules(details.stream()
                        .map(ConcertScheduleResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}