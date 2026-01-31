package com.umc.connext.global.auth.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.service.TermService;
import com.umc.connext.global.auth.dto.JoinLocalRequestDTO;
import com.umc.connext.domain.member.service.NicknameService;
import com.umc.connext.global.auth.dto.JoinSocialRequestDTO;
import com.umc.connext.global.oauth2.enums.SocialType;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final NicknameService nicknameService;
    private final TermService termService;

    @Transactional
    public Member joinLocal(JoinLocalRequestDTO joinLocalRequestDTO){

        termService.validateRequiredTerms(joinLocalRequestDTO.getAgreedTermIds());

        String email = joinLocalRequestDTO.getEmail();
        String password = joinLocalRequestDTO.getPassword();

        //자체 회원가입 계정 존재 체크
        if(memberRepository.existsBySocialTypeAndProviderId(SocialType.LOCAL, email)){
            throw new GeneralException(ErrorCode.ID_ALREADY_EXISTS,"이미 가입된 이메일입니다.");
        }

        //소셜 계정 이메일과 충돌 체크
        if (memberRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorCode.EMAIL_ALREADY_USED_BY_SOCIAL, "이미 소셜 계정으로 가입된 이메일입니다.");
        }

        Member member = Member.local(email, bCryptPasswordEncoder.encode(password), nicknameService.generateRandomNickname());
        memberRepository.save(member);
        termService.saveAgreements(member, joinLocalRequestDTO.getAgreedTermIds());

        return member;
    }

    @Transactional
    public Member joinSocial(Long memberId , JoinSocialRequestDTO joinSocialRequestDTO){

        termService.validateRequiredTerms(joinSocialRequestDTO.getAgreedTermIds());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER, "해당 사용자를 찾을 수 없습니다."));
        member.updateMemberStatus(MemberStatus.ACTIVE);

        termService.saveAgreements(member, joinSocialRequestDTO.getAgreedTermIds());

        return member;
    }

    @Transactional
    public void withdrawCurrentUser(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER,"존재하지 않는 회원입니다."));

        //Refresh Token 전부 제거
        refreshTokenService.removeAllByAuthKey(member.getId());

        //삭제
        memberRepository.delete(member);
    }
}

