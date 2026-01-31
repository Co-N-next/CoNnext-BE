package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.global.oauth2.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByNickname(String nickname);
    boolean existsBySocialTypeAndProviderId(SocialType socialType, String providerId);
    boolean existsByEmail(String email);
    Optional<Member> findBySocialTypeAndProviderId(SocialType socialType, String providerId);

    boolean existsByEmailAndSocialType(String email, SocialType socialType);
    Optional<Member> findByEmail(String email);
}
