package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public void checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorCode.ID_ALREADY_EXISTS, "이미 가입된 이메일입니다.");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일은 필수입니다.");
        }

        if (email.length() > 50) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일은 최대 50자까지 가능합니다.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일 형식이 올바르지 않습니다.");
        }
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER, "해당 사용자를 찾을 수 없습니다."));
    }
}
