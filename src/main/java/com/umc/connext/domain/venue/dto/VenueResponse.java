package com.umc.connext.domain.venue.dto;

import com.umc.connext.domain.venue.entity.VenueFacility;
import com.umc.connext.domain.venue.entity.VenueSection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "VenueResponse",
        description = "공연장 상세 정보 및 지도 데이터 응답 DTO"
)
public class VenueResponse {

    @Schema(description = "공연장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long venueId;

    @Schema(description = "공연장 이름", example = "올림픽홀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "공연장 주소", example = "서울 송파구 올림픽로 424")
    private String address;

    @Schema(description = "총 층수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalFloors;

    @Schema(description = "공연장 조회수", example = "1523")
    private Long totalViews;

    @Schema(description = "SVG 지도 가로 크기", example = "1920")
    private Integer svgWidth;

    @Schema(description = "SVG 지도 세로 크기", example = "1080")
    private Integer svgHeight;

    @Schema(description = "층별 지도 데이터", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FloorData> floors;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "VenueFloorData", description = "공연장 단일 층 지도 데이터")
    public static class FloorData {

        @Schema(description = "층 번호", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer floor;

        @Schema(description = "구역(벽/좌석/무대 등) 목록")
        private List<SectionDto> sections;

        @Schema(description = "시설물(화장실/계단 등) 목록")
        private List<FacilityDto> facilities;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "VenueSection", description = "공연장 구역(섹션) 정보")
    public static class SectionDto {

        @Schema(description = "섹션 ID", example = "A")
        private String sectionId;

        @Schema(description = "섹션 타입", example = "SEAT")
        private String type;

        @Schema(description = "SVG Path 데이터", example = "M10 10 L50 10 L50 50 Z")
        private String pathData;

        public static SectionDto from(VenueSection section) {
            return SectionDto.builder()
                    .sectionId(section.getSectionId())
                    .type(section.getType().toString())
                    .pathData(section.getFullPath())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "VenueFacility", description = "공연장 시설물 정보")
    public static class FacilityDto {

        @Schema(description = "시설물 ID", example = "10")
        private Long facilityId;

        @Schema(description = "시설물 타입", example = "TOILET")
        private String type;

        @Schema(description = "시설물 이름", example = "여자 화장실")
        private String name;

        @Schema(description = "층 정보 (검색 시 필요)", example = "1")
        private Integer floor;

        @Schema(description = "X 좌표", example = "345.67")
        private BigDecimal x;

        @Schema(description = "Y 좌표", example = "789.12")
        private BigDecimal y;

        public static FacilityDto from(VenueFacility facility) {
            return FacilityDto.builder()
                    .facilityId(facility.getId())
                    .type(facility.getType().toString())
                    .name(facility.getName())
                    .floor(facility.getFloor())
                    .x(facility.getX())
                    .y(facility.getY())
                    .build();
        }
    }

    @Builder
    public record VenuePreviewDTO(
            Long id,
            String name,
            String city,
            String imageUrl
    ){}
}