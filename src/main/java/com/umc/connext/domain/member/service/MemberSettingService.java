package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.dto.NotificationSettingRequestDTO;
import com.umc.connext.domain.member.dto.NotificationSettingResponseDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingRequestDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingResponseDTO;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberNotificationSetting;
import com.umc.connext.domain.member.entity.MemberVisibilitySetting;
import com.umc.connext.domain.member.repository.MemberNotificationSettingRepository;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.repository.MemberVisibilitySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSettingService {

    private final MemberRepository memberRepository;
    private final MemberNotificationSettingRepository memberNotificationSettingRepository;
    private final MemberVisibilitySettingRepository memberVisibilitySettingRepository;

    @Transactional
    public VisibilitySettingResponseDTO getVisibility(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER,"존재하지 않는 회원입니다."));


        MemberVisibilitySetting setting =
                memberVisibilitySettingRepository.findByMember(member)
                        .orElseGet(() -> memberVisibilitySettingRepository.save(MemberVisibilitySetting.from(member)));

        return VisibilitySettingResponseDTO.from(setting);
    }

    @Transactional
    public void updateVisibility(Long memberId, VisibilitySettingRequestDTO req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER,"존재하지 않는 회원입니다."));


        MemberVisibilitySetting setting =
                memberVisibilitySettingRepository.findByMember(member)
                        .orElseGet(() -> memberVisibilitySettingRepository.save(MemberVisibilitySetting.from(member)));

        setting.update(req.getPerformanceVisibility(), req.getSeatVisibility());
    }

    @Transactional
    public NotificationSettingResponseDTO getNotification(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER,"존재하지 않는 회원입니다."));

        MemberNotificationSetting setting =
                memberNotificationSettingRepository.findByMember(member)
                        .orElseGet(() -> memberNotificationSettingRepository.save(MemberNotificationSetting.from(member)));

        return NotificationSettingResponseDTO.from(setting);
    }

    @Transactional
    public void updateNotification(Long memberId, NotificationSettingRequestDTO req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER,"존재하지 않는 회원입니다."));

        MemberNotificationSetting setting =
                memberNotificationSettingRepository.findByMember(member)
                        .orElseGet(() -> memberNotificationSettingRepository.save(MemberNotificationSetting.from(member)));

        setting.update(req.isServiceEnabled(), req.isPushEnabled(), req.isSmsEnabled());
    }
}
