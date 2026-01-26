package com.umc.connext.domain.venue.dto;

import com.umc.connext.common.response.PageInfo;
import lombok.Builder;

import java.util.List;

public class VenueResDTO {

    @Builder
    public record VenuePreviewDTO(
            Long id,
            String name,
            String city,
            String imageUrl
    ){}

}
