package com.umc.connext.domain.venue.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraphStatisticsResponse {
    private Long venueId;
    private int sectionCount;
    private int facilityCount;
    private int nodeCount;
    private int edgeCount;
}