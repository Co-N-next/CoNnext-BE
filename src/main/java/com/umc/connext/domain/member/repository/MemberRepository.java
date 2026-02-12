package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.global.oauth2.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByEmailAndSocialType(String email, SocialType socialType);
    Optional<Member> findByEmail(String email);

    @Query("""
        SELECT m
        FROM Member m
        WHERE m.email = :email
          AND m.socialType = :socialType
    """)
    Optional<Member> findIncludingDeletedByEmailAndSocialType(
            @Param("email") String email,
            @Param("socialType") SocialType socialType
    );

    @Query("""
        SELECT m
        FROM Member m
        WHERE m.socialType = :socialType
          AND m.providerId = :providerId
    """)
    Optional<Member> findIncludingDeletedBySocialTypeAndProviderId(
            @Param("socialType") SocialType socialType,
            @Param("providerId") String providerId
    );

    List<Member> findAllByMemberStatusAndDeletedAtBefore(MemberStatus memberStatus, LocalDateTime threshold);

    @Query("""
        SELECT m.id
        FROM Member m
        WHERE m.memberStatus = 'ACTIVE'
    """)
    List<Long> findAllActiveMemberIds();

    @Modifying
    @Query("""
        DELETE FROM Member m
        WHERE m.memberStatus = 'DELETED'
        AND m.deletedAt < :threshold
    """)
    void hardDeletedMembers(@Param("threshold") LocalDateTime threshold);

    List<Long> findIdsByMemberStatusAndDeletedAtBefore(MemberStatus memberStatus, LocalDateTime threshold);
}