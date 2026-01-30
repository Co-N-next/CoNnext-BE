package com.umc.connext.global.auth.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.enums.Role;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.service.TermService;
import com.umc.connext.global.auth.dto.LocalJoinDTO;
import com.umc.connext.domain.member.service.NicknameService;
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
    public void join(LocalJoinDTO localJoinDTO){

        termService.validateRequiredTerms(localJoinDTO.getAgreedTermIds());

        String username = localJoinDTO.getUsername();
        String password = localJoinDTO.getPassword();

        // 1️ 자체 회원가입 계정 존재 체크
        if(memberRepository.existsByUsername(username)){
            throw new GeneralException(ErrorCode.ID_ALREADY_EXISTS,"이미 가입된 이메일입니다.");
        }

        // 2️ 소셜 계정 이메일과 충돌 체크
        memberRepository.findByEmail(username).ifPresent(member -> {
            throw new GeneralException(ErrorCode.EMAIL_ALREADY_USED_BY_SOCIAL,"이미 다른 소셜 계정으로 가입된 이메일입니다.");
        });

        // 3️ 회원 생성
        Member member = Member.of(username, username, bCryptPasswordEncoder.encode(password), nicknameService.generateRandomNickname(), Role.ROLE_USER);
        memberRepository.save(member);
        termService.saveAgreements(member, localJoinDTO.getAgreedTermIds());
    }

    @Transactional
    public void withdrawCurrentUser(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_MEMBER_FOUND,"존재하지 않는 회원입니다."));

        // 1️ Refresh Token 전부 제거 (즉시 로그아웃)
        refreshTokenService.removeAllByAuthKey(member.getUsername());

        // 2️ 삭제
        memberRepository.delete(member);
    }
}

