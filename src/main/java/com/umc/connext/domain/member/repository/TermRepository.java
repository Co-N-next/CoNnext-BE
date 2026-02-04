package com.umc.connext.domain.member.repository;

import com.umc.connext.domain.member.entity.Term;
import com.umc.connext.domain.member.enums.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {
    List<Term> findByTypeAndActive(TermType type, boolean active);
}
