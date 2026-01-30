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
    public record NearbyVenueDTO(
            Long id,
            String name
    ){}

}
