package com.umc.connext.domain.venue.entity;

import com.umc.connext.common.enums.SectionType;
import com.umc.connext.domain.venue.dto.Coordinate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j

@Entity
@Table(
        name = "venue_sections",
        indexes = {
                @Index(name = "idx_venue_section_floor", columnList = "venue_id, floor")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "VenueSection",
        description = "공연장 구역(섹션) 엔티티 (벽, 좌석, 무대 등 SVG 기반 영역)"
)
public class VenueSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "섹션 DB ID", example = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    @Schema(description = "공연장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long venueId;

    @Column(name = "floor", nullable = false)
    @Schema(description = "섹션이 위치한 층", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer floor;

    @Column(name = "section_id", length = 50)
    @Schema(description = "섹션 ID (SVG 기준 식별자)", example = "A")
    private String sectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    @Schema(
            description = "섹션 타입",
            implementation = SectionType.class,
            example = "SEAT"
    )
    private SectionType type;

    @Column(name = "full_path", columnDefinition = "TEXT")
    @Schema(
            description = "섹션 전체 SVG Path 데이터",
            example = "M10 10 L50 10 L50 50 Z"
    )
    private String fullPath;

    @Column(name = "vertices", columnDefinition = "JSON")
    @Schema(
            description = "섹션 꼭짓점 좌표(JSON 문자열). Coordinate 배열 또는 [x,y] 배열 형식 지원",
            example = "[{\"x\":100,\"y\":200},{\"x\":300,\"y\":400}]"
    )
    private String vertices;

    @Column(name = "center_x", precision = 10, scale = 1)
    @Schema(description = "섹션 중심 X 좌표", example = "200.0")
    private BigDecimal centerX;

    @Column(name = "center_y", precision = 10, scale = 1)
    @Schema(description = "섹션 중심 Y 좌표", example = "300.0")
    private BigDecimal centerY;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Transient
    public List<Coordinate> getVerticesList() {
        if (vertices == null || vertices.isEmpty()) {
            log.warn("⚠️ 섹션 [ID: {}, SectionId: {}] vertices 데이터가 NULL 또는 비어있습니다!", id, sectionId);
            return new ArrayList<>();
        }

        log.debug("섹션 [ID: {}, SectionId: {}] vertices 파싱 시작 - 길이: {}", id, sectionId, vertices.length());
        log.trace("vertices 원본 데이터: {}", vertices);

        try {
            List<Coordinate> result = OBJECT_MAPPER.readValue(vertices, new TypeReference<List<Coordinate>>() {});
            log.debug("섹션 [ID: {}, SectionId: {}] Coordinate 파싱 성공 - 좌표 개수: {}", id, sectionId, result.size());
            return result;
        } catch (Exception e1) {
            log.warn("섹션 [ID: {}, SectionId: {}] Coordinate 파싱 실패, 대체 방법 시도", id, sectionId, e1);

            try {
                List<List<BigDecimal>> rawList = OBJECT_MAPPER.readValue(
                        vertices, new TypeReference<List<List<BigDecimal>>>() {});
                List<Coordinate> result = rawList.stream()
                        .filter(arr -> arr.size() >= 2)
                        .map(arr -> new Coordinate(arr.get(0), arr.get(1)))
                        .toList();
                log.debug("섹션 [ID: {}, SectionId: {}] 배열 형식 파싱 성공 - 좌표 개수: {}", id, sectionId, result.size());
                return result;
            } catch (Exception e2) {
                log.error("섹션 [ID: {}, SectionId: {}] 모든 파싱 방법 실패! vertices: {}", id, sectionId, vertices, e2);
                return new ArrayList<>();
            }
        }
    }

    public void setVerticesList(List<Coordinate> coordinateList) {
        try {
            this.vertices = OBJECT_MAPPER.writeValueAsString(coordinateList);
        } catch (Exception e) {
            this.vertices = "[]";
        }
    }
}
