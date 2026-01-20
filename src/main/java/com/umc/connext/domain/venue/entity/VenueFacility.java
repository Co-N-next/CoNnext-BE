package com.umc.connext.domain.venue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venue_facilities", indexes = {
        @Index(name = "idx_venue_type", columnList = "venue_id, type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "type", length = 20)
    private String type; // VIP_TOILET, OFFICE, STORE, TOILET, STAIRS, ETC

    @Column(name = "x", precision = 10, scale = 1, nullable = false)
    private BigDecimal x;

    @Column(name = "y", precision = 10, scale = 1, nullable = false)
    private BigDecimal y;

    /**
     * 시설물 타입 Enum
     */
    public enum FacilityType {
        VIP_TOILET("VIP 공용 화장실"),
        OFFICE("운영사무실"),
        STORE("기념품판매대"),
        TOILET("화장실"),
        STAIRS("계단"),
        ETC("기타");

        private final String description;

        FacilityType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static FacilityType fromString(String type) {
            try {
                return FacilityType.valueOf(type);
            } catch (Exception e) {
                return ETC;
            }
        }
    }

    @Transient
    public FacilityType getFacilityType() {
        return FacilityType.fromString(this.type);
    }
}
