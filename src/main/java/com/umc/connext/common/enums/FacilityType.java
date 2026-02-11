package com.umc.connext.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "FacilityType",
        description = "공연장 시설물 타입"
)
public enum FacilityType {

    @Schema(description = "VIP 공용 화장실")
    VIP_TOILET("VIP 공용 화장실"),

    @Schema(description = "운영 사무실")
    OFFICE("운영사무실"),

    @Schema(description = "기념품 판매대")
    STORE("기념품판매대"),

    @Schema(description = "일반 화장실")
    TOILET("화장실"),

    @Schema(description = "계단")
    STAIRS("계단"),

    @Schema(description = "엘리베이터")
    ELEVATOR("엘리베이터"),

    @Schema(description = "입구")
    ENTRANCE("입구"),

    @Schema(description = "출구")
    EXIT("출구"),

    @Schema(description = "기타")
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
