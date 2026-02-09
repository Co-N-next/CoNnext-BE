package com.umc.connext.domain.venue.converter;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import com.umc.connext.domain.venue.entity.FavoriteVenue;
import com.umc.connext.domain.venue.entity.Venue;
import com.umc.connext.domain.venue.projection.SimpleVenue;
import com.umc.connext.domain.venue.projection.SearchVenue;

public class VenueConverter {

    public static VenueResDTO.VenuePreviewDTO toVenuePreviewDTO(Venue venue) {
        return VenueResDTO.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }

    public static VenueResDTO.VenueSimpleDTO toVenueSimpleDTO(Venue venue) {
        return VenueResDTO.VenueSimpleDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .build();
    }

    public static VenueResDTO.VenueSimpleDTO toVenueSimpleDTO(SimpleVenue venue) {
        return VenueResDTO.VenueSimpleDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .build();
    }

    public static FavoriteVenue toFavoriteVenue(Member member, Venue venue) {
        return FavoriteVenue.builder()
                .member(member)
                .venue(venue)
                .build();
    }

    public static VenueResDTO.VenuePreviewDTO toVenuePreviewDTO(SearchVenue venue) {
        return VenueResDTO.VenuePreviewDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .city(venue.getCity())
                .imageUrl(venue.getImageUrl())
                .build();
    }
}