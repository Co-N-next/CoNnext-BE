package com.umc.connext.domain.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class VenueRequest {

    @Schema(
            name = "VenueCreateRequest",
            description = "공연장 생성 요청 DTO"
    )
    public record VenueCreateRequest(

            @Schema(
                    description = "공연장명",
                    example = "올림픽홀",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String name,

            @Schema(
                    description = "주소",
                    example = "서울 송파구 올림픽로 424"
            )
            String address,

            @Schema(
                    description = "총 층수",
                    example = "3",
                    defaultValue = "1"
            )
            Integer totalFloors
    ) {}

    @Schema(
            name = "VenueUpdateRequest",
            description = "공연장 수정 요청 DTO (null 필드는 변경하지 않음)"
    )
    public record VenueUpdateRequest(

            @Schema(
                    description = "공연장명",
                    example = "올림픽홀"
            )
            String name,

            @Schema(
                    description = "주소",
                    example = "서울 송파구 올림픽로 424"
            )
            String address,

            @Schema(
                    description = "총 층수",
                    example = "3"
            )
            Integer totalFloors,

            @Schema(
                    description = "활성 여부",
                    example = "true"
            )
            Boolean isActive
    ) {}

    @Schema(
            name = "FloorConfigRequest",
            description = "공연장 섹션별 층 설정 요청 DTO"
    )
    public record FloorConfigRequest(

            @Schema(
                    description = "섹션 ID",
                    example = "A",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String sectionId,

            @Schema(
                    description = "층 번호",
                    example = "1",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Integer floor,

            @Schema(
                    description = "설명",
                    example = "1층 A구역"
            )
            String description
    ) {}
}
