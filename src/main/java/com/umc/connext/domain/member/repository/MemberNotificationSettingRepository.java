package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberNotificationSettingRepository  extends JpaRepository<MemberNotificationSetting, Long> {
    Optional<MemberNotificationSetting> findByMember(Member member);
}
