package com.umc.connext.domain.venue.converter;

import com.umc.connext.domain.venue.dto.VenueResponse;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.projection.SearchVenue;

public class VenueConverter {

    // Entity -> DTO
    public static VenueResponse.VenuePreviewDTO toVenuePreviewDTO(
            Venue venue
    ){
        return VenueResponse.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }

    // projection -> DTO
    public static VenueResponse.VenuePreviewDTO toVenuePreviewDTO(SearchVenue venue) {
        return VenueResponse.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }

}
