package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Integer> {

    Boolean existsByUsername(String username);
    Boolean existsByNickname(String nickname);
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
}
