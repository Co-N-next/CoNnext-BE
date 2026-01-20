package com.umc.connext.domain.venue.dto;

import com.umc.connext.domain.venue.entity.VenueFacility;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueFacilityResponse {
    private Long id;
    private Long venueId;
    private String name;
    private String type;
    private String typeDescription;
    private BigDecimal x;
    private BigDecimal y;
    private Double distance; // 검색 기준점으로부터의 거리

    public static VenueFacilityResponse from(VenueFacility facility) {
        return VenueFacilityResponse.builder()
                .id(facility.getId())
                .venueId(facility.getVenueId())
                .name(facility.getName())
                .type(facility.getType())
                .typeDescription(facility.getFacilityType().getDescription())
                .x(facility.getX())
                .y(facility.getY())
                .build();
    }
}