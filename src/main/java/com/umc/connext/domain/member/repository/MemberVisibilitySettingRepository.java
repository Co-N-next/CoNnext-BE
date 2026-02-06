package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberVisibilitySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MemberVisibilitySettingRepository extends JpaRepository<MemberVisibilitySetting, Long> {
    Optional<MemberVisibilitySetting> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM MemberVisibilitySetting mvs WHERE mvs.member.id IN :memberIds")
    void deleteByMemberIds(@Param("memberIds") List<Long> memberIds);
}
