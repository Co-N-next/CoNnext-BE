package com.umc.connext.domain.venue.dto;

import com.umc.connext.domain.venue.entity.VenueSection;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueSectionResponse {
    private Long id;
    private Long venueId;
    private String sectionId;
    private String fullPath;
    private List<Coordinate> vertices;
    private BigDecimal centerX;
    private BigDecimal centerY;

    public static VenueSectionResponse from(VenueSection section) {
        return VenueSectionResponse.builder()
                .id(section.getId())
                .venueId(section.getVenueId())
                .sectionId(section.getSectionId())
                .fullPath(section.getFullPath())
                .vertices(section.getVerticesList())
                .centerX(section.getCenterX())
                .centerY(section.getCenterY())
                .build();
    }
}