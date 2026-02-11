package com.umc.connext.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "알림 설정 변경 요청 DTO")
public class NotificationSettingRequestDTO {

    @Schema(
            description = "서비스 이용 알림 수신 여부 (마스터 스위치)",
            example = "true"
    )
    private Boolean serviceEnabled;

    @Schema(
            description = "푸시 알림 수신 여부",
            example = "true"
    )
    private Boolean pushEnabled;

    @Schema(
            description = "문자(SMS) 알림 수신 여부",
            example = "false"
    )
    private Boolean smsEnabled;
}
