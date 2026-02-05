package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberVisibilitySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberVisibilitySettingRepository extends JpaRepository<MemberVisibilitySetting, Long> {
    Optional<MemberVisibilitySetting> findByMember(Member member);
}
