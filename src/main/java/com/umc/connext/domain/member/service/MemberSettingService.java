package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.dto.NotificationSettingRequestDTO;
import com.umc.connext.domain.member.dto.NotificationSettingResponseDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingRequestDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingResponseDTO;
import com.umc.connext.domain.member.entity.MemberNotificationSetting;
import com.umc.connext.domain.member.entity.MemberVisibilitySetting;
import com.umc.connext.domain.member.repository.MemberNotificationSettingRepository;
import com.umc.connext.domain.member.repository.MemberVisibilitySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSettingService {

    private final MemberNotificationSettingRepository memberNotificationSettingRepository;
    private final MemberVisibilitySettingRepository memberVisibilitySettingRepository;

    @Transactional(readOnly = true)
    public VisibilitySettingResponseDTO getVisibility(Long memberId) {

        MemberVisibilitySetting setting = memberVisibilitySettingRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "공개 범위 설정이 존재하지 않습니다."));

        return VisibilitySettingResponseDTO.from(setting);
    }

    @Transactional
    public void updateVisibility(Long memberId, VisibilitySettingRequestDTO req) {

        MemberVisibilitySetting setting =   memberVisibilitySettingRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "공개 범위 설정이 존재하지 않습니다."));

        setting.update(req.getPerformanceVisibility(), req.getSeatVisibility());
    }

    @Transactional
    public NotificationSettingResponseDTO getNotification(Long memberId) {

        MemberNotificationSetting setting = memberNotificationSettingRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "알림 설정이 존재하지 않습니다."));

        return NotificationSettingResponseDTO.from(setting);
    }

    @Transactional
    public void updateNotification(Long memberId, NotificationSettingRequestDTO req) {

        MemberNotificationSetting setting = memberNotificationSettingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "알림 설정이 존재하지 않습니다."));

        setting.update(req.getServiceEnabled(), req.getPushEnabled(), req.getSmsEnabled());
    }
}
