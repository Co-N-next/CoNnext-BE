package com.umc.connext.domain.venue.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Venue",
        description = "공연장 기본 엔티티"
)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "공연장 ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    @Schema(description = "공연장 이름", example = "올림픽홀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(name = "address", length = 500)
    @Schema(description = "공연장 주소", example = "서울 송파구 올림픽로 424")
    private String address;

    @Column(name = "total_floors", nullable = false)
    @Builder.Default
    @Schema(description = "총 층 수", example = "3", defaultValue = "1")
    private Integer totalFloors = 1;

    @Column(name = "total_views", nullable = false)
    @Builder.Default
    @Schema(description = "총 조회수", example = "1523", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalViews = 0L;

    @Column(name = "search_count", nullable = false)
    @Builder.Default
    @Schema(description = "검색 횟수", example = "321", accessMode = Schema.AccessMode.READ_ONLY)
    private Long searchCount = 0L;

    @Column(name = "svg_width")
    @Schema(description = "SVG 지도 가로 크기", example = "1920")
    private Integer svgWidth;

    @Column(name = "svg_height")
    @Schema(description = "SVG 지도 세로 크기", example = "1080")
    private Integer svgHeight;

    @Column(name = "svg_path", length = 500)
    @Schema(description = "원본 SVG 파일 경로 또는 URL", example = "/static/maps/venue_1.svg")
    private String svgPath;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @Schema(description = "활성 상태", example = "true")
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    @Schema(description = "생성 시각", example = "2024-01-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Schema(description = "수정 시각", example = "2024-01-10T18:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
