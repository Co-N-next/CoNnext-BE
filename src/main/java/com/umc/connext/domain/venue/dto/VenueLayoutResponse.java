package com.umc.connext.domain.venue.dto;

import com.umc.connext.common.enums.SectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연장 레이아웃 응답")
public class VenueLayoutResponse {

    @Schema(description = "공연장 기본 정보")
    private VenueInfo venue;

    @Schema(description = "섹션 목록")
    private List<SectionInfo> sections;

    @Schema(description = "시설물 목록")
    private List<FacilityInfo> facilities;

    @Schema(description = "사용 가능한 층 목록")
    private List<Integer> floors;

    // ===== 내부 클래스 =====

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "공연장 기본 정보")
    public static class VenueInfo {
        @Schema(description = "공연장 ID", example = "1")
        private Long venueId;

        @Schema(description = "공연장 이름", example = "세종문화회관 대극장")
        private String name;

        @Schema(description = "공연장 주소", example = "서울특별시 종로구 세종대로 175")
        private String address;

        @Schema(description = "SVG 너비", example = "3000")
        private Integer svgWidth;

        @Schema(description = "SVG 높이", example = "2000")
        private Integer svgHeight;

        @Schema(description = "총 층수", example = "2")
        private Integer totalFloors;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "섹션 정보")
    public static class SectionInfo {
        @Schema(description = "섹션 ID", example = "33")
        private String sectionId;

        @Schema(description = "층 번호", example = "1")
        private Integer floor;

        @Schema(description = "SVG path 데이터 (프론트엔드에서 <path d='...'> 렌더링용)",
                example = "M 2041.7,2630.9 L 2050.0,2640.0 Z")
        private String svgPath;

        @Schema(description = "섹션 중심 X 좌표", example = "2100.5")
        private BigDecimal centerX;

        @Schema(description = "섹션 중심 Y 좌표", example = "2700.3")
        private BigDecimal centerY;

        @Schema(description = "섹션 타입", example = "SEAT")
        private SectionType type;

        @Schema(description = "섹션 꼭짓점 좌표 리스트 (선택적)")
        private List<Coordinate> vertices;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "시설물 정보")
    public static class FacilityInfo {
        @Schema(description = "시설물 ID", example = "1")
        private Long id;

        @Schema(description = "시설물 이름", example = "남측 계단")
        private String name;

        @Schema(description = "시설물 타입", example = "STAIRS")
        private String type;

        @Schema(description = "층 번호", example = "1")
        private Integer floor;

        @Schema(description = "X 좌표", example = "182.0")
        private BigDecimal x;

        @Schema(description = "Y 좌표", example = "441.0")
        private BigDecimal y;

        @Schema(description = "연결된 층 정보 (계단/엘리베이터용)", example = "1,2")
        private String connectedFloors;
    }
}