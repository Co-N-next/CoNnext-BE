package com.umc.connext.domain.venue.dto;

import lombok.Builder;

public class VenueResDTO {

    @Builder
    public record VenuePreviewDTO(
            Long id,
            String name,
            String city,
            String imageUrl
    ){}

    @Builder
    public record VenueSimpleDTO(
            Long id,
            String name
    ){}

}
