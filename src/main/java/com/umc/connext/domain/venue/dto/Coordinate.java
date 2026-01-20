package com.umc.connext.domain.venue.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinate {
    private BigDecimal x;
    private BigDecimal y;

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }
}