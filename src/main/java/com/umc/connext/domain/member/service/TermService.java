package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.dto.ActiveTermResponseDTO;
import com.umc.connext.domain.member.dto.MyTermResponseDTO;
import com.umc.connext.domain.member.dto.OptionalTermsChangeRequestDTO;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.entity.MemberTerm;
import com.umc.connext.domain.member.entity.Term;
import com.umc.connext.domain.member.enums.TermType;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.repository.MemberTermRepository;
import com.umc.connext.domain.member.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final MemberTermRepository memberTermRepository;
    private final MemberRepository memberRepository;

    public void validateRequiredTerms(List<Long> agreedTermIds) {

        List<Term> requiredTerms =
                termRepository.findByTypeAndActive(TermType.REQUIRED, true);

        boolean allAgreed = requiredTerms.stream()
                .allMatch(term -> agreedTermIds.contains(term.getId()));

        if (!allAgreed) {
            throw new GeneralException(ErrorCode.MISSING_REQUIRED_TERM, "필수 약관에 동의해야합니다.");
        }
    }

    @Transactional
    public void saveAgreements(Member member, List<Long> termIds) {
        List<Term> allTerms = termRepository.findAll();
        Set<Long> agreedTermIds = new HashSet<>(termIds);

        List<MemberTerm> memberTerms = allTerms.stream()
                .map(term -> {
                    boolean isAgreed = agreedTermIds.contains(term.getId());

                    if (term.getType() == TermType.REQUIRED && !isAgreed) {
                        throw new GeneralException(ErrorCode.MISSING_REQUIRED_TERM, "필수 약관에 동의해야합니다.");
                    }

                    return MemberTerm.of(member, term, isAgreed);
                })
                .toList();

        memberTermRepository.saveAll(memberTerms);
    }

    public List<ActiveTermResponseDTO> getActiveTerms() {
        return termRepository.findAll().stream()
                .filter(Term::isActive)
                .map(ActiveTermResponseDTO::from)
                .toList();
    }

    public List<MyTermResponseDTO> getMyOptionalTerms(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER, "존재하지 않는 회원입니다."));

        return memberTermRepository.findByMemberAndTerm_Type(member, TermType.OPTIONAL).stream()
                .map(MyTermResponseDTO::from)
                .toList();
    }

    @Transactional
    public void changeOptionalTerms(Long memberId, List<OptionalTermsChangeRequestDTO.TermAgreement> agreements) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_DELETED, "존재하지 않는 회원입니다."));
        for (var agreement : agreements) {
            Term term = termRepository.findById(agreement.getTermId())
                    .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "존재하지 않는 약관입니다."));

            // 선택 약관만 변경 가능
            if (term.getType() != TermType.OPTIONAL) {
                throw new GeneralException(ErrorCode.INVALID_TERM_TYPE, "필수 약관은 수정할 수 없습니다.");
            }

            MemberTerm memberTerm = memberTermRepository
                    .findByMemberAndTerm(member, term)
                    .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "회원의 약관 동의 정보가 없습니다."));

            memberTerm.changeAgreement(agreement.getAgreed());
        }
    }
}
