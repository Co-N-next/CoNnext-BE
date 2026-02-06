package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.entity.MemberNotificationSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "알림설정 조회 DTO")
@Builder
public class NotificationSettingResponseDTO {

    @Schema(
            description = "서비스 이용 알림 수신 여부",
            example = "true"
    )
    private boolean serviceEnabled;

    @Schema(
            description = "푸시 알림 수신 여부",
            example = "true"
    )
    private boolean pushEnabled;

    @Schema(
            description = "문자(SMS) 알림 수신 여부",
            example = "false"
    )
    private boolean smsEnabled;

    public static NotificationSettingResponseDTO from(MemberNotificationSetting setting) {
        return NotificationSettingResponseDTO.builder()
                .serviceEnabled(setting.isServiceNotificationEnabled())
                .pushEnabled(setting.isPushNotificationEnabled())
                .smsEnabled(setting.isSmsNotificationEnabled())
                .build();
    }
}
