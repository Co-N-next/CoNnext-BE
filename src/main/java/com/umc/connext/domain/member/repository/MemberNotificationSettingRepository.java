package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MemberNotificationSettingRepository  extends JpaRepository<MemberNotificationSetting, Long> {
    Optional<MemberNotificationSetting> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM MemberNotificationSetting mns WHERE mns.member.id IN :memberIds")
    void deleteByMemberIds(@Param("memberIds") List<Long> memberIds);

}
