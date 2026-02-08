package com.umc.connext.domain.member.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member_term")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTerm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Term term;

    @Column(nullable = false)
    private boolean isAgreed;

    public static MemberTerm of(Member member, Term term, boolean isAgreed  ) {
        MemberTerm memberTerm = new MemberTerm();
        memberTerm.member = member;
        memberTerm.term = term;
        memberTerm.isAgreed = isAgreed;
        return memberTerm;
    }

    public void changeAgreement(boolean isAgreed) {
        this.isAgreed = isAgreed;
    }
}
