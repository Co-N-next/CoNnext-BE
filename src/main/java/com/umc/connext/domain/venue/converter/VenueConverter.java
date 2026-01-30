package com.umc.connext.domain.venue.converter;

import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.projection.NearbyVenue;
import com.umc.connext.domain.venue.projection.SearchVenue;

public class VenueConverter {

    // Entity -> DTO
    public static VenueResDTO.VenuePreviewDTO toVenuePreviewDTO(
            Venue venue
    ){
        return VenueResDTO.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }

    public static VenueResDTO.VenueSimpleDTO toNearByVenueDTO(
            NearbyVenue venue
    ){
        return VenueResDTO.VenueSimpleDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .build();
    }

    // projection -> DTO
    public static VenueResDTO.VenuePreviewDTO toVenuePreviewDTO(SearchVenue venue) {
        return VenueResDTO.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }

}
