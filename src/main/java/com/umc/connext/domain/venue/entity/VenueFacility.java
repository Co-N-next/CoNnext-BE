package com.umc.connext.domain.venue.entity;

import com.umc.connext.common.enums.FacilityType;
import com.umc.connext.domain.concert.entity.ConcertVenue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "venue_facilities", indexes = {
        @Index(name = "idx_venue_facility_type", columnList = "venue_id, type"),
        @Index(name = "idx_venue_facility_floor", columnList = "venue_id, floor")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@Schema(
        name = "VenueFacility",
        description = "공연장 시설물 엔티티 (화장실/계단/출입구 등)"
)
public class VenueFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "시설물 ID", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    @Schema(description = "공연장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long venueId;

    @Column(name = "floor", nullable = false)
    @Schema(description = "시설물이 위치한 층", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer floor;

    @Column(name = "name", length = 100)
    @Schema(description = "시설물 이름", example = "여자 화장실")
    private String name;

    @Column(name = "type", length = 20, nullable = false)
    @Schema(
            description = "시설물 타입 (FacilityType)",
            example = "TOILET",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @Column(name = "x", precision = 10, scale = 1, nullable = false)
    @Schema(description = "X 좌표", example = "345.7", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal x;

    @Column(name = "y", precision = 10, scale = 1, nullable = false)
    @Schema(description = "Y 좌표", example = "890.1", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal y;

    @Column(name = "connected_floors", length = 50)
    @Schema(
            description = "연결된 층 목록(콤마 구분 문자열, 계단/엘리베이터 등 층전환 시설 전용). 예: \"1,2\"",
            example = "1,2"
    )
    private String connectedFloors;

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    private List<ConcertVenue> concertVenues = new ArrayList<>();

    @Transient
    public FacilityType getFacilityType() {
        return FacilityType.fromString(this.type);
    }

    @Transient
    public boolean isStairs() {
        return FacilityType.STAIRS.name().equals(this.type);
    }

    @Transient
    public boolean connectsFloor(Integer floor) {
        if (connectedFloors == null || connectedFloors.isEmpty()) {
            return false;
        }
        return getConnectedFloorsList().contains(floor);
    }

    @Transient
    public List<Integer> getConnectedFloorsList() {
        if (connectedFloors == null || connectedFloors.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(connectedFloors.split(","))
                .map(String::trim)
                .map(this::tryParseInteger)
                .filter(floor -> floor != null)
                .toList();
    }

    private Integer tryParseInteger(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            log.warn("Failed to parse floor number from '{}', skipping", value);
            return null;
        }
    }

    public void setConnectedFloorsList(List<Integer> floors) {
        if (floors == null || floors.isEmpty()) {
            this.connectedFloors = null;
        } else {
            this.connectedFloors = floors.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(null);
        }
    }

    @Transient
    public boolean connectsFloors(Integer floor1, Integer floor2) {
        List<Integer> floors = getConnectedFloorsList();
        return floors.contains(floor1) && floors.contains(floor2);
    }
}
