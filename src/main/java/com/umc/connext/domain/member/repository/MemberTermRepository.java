package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberTerm;
import com.umc.connext.domain.member.entity.Term;
import com.umc.connext.domain.member.enums.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {
    List<MemberTerm> findByMemberAndTerm_Type(Member member, TermType termType);
    Optional<MemberTerm> findByMemberAndTerm(Member member, Term term);
}
