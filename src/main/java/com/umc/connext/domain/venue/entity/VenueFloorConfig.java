package com.umc.connext.domain.venue.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "venue_floor_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "section_id"}),
        indexes = {
                @Index(name = "idx_venue_floor_config", columnList = "venue_id, floor")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "VenueFloorConfig",
        description = "공연장별 섹션-층 매핑 엔티티 (섹션 ID가 어느 층에 속하는지 관리)"
)
public class VenueFloorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "층 설정 ID",
            example = "100",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(name = "venue_id", nullable = false)
    @Schema(
            description = "공연장 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long venueId;

    @Column(name = "section_id", length = 50, nullable = false)
    @Schema(
            description = "섹션 ID",
            example = "A",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String sectionId;

    @Column(name = "floor", nullable = false)
    @Schema(
            description = "섹션이 속한 층 번호",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer floor;

    @Column(name = "description", length = 200)
    @Schema(
            description = "섹션 설명 (옵션)",
            example = "오른쪽 2층 VIP 구역"
    )
    private String description;
}
