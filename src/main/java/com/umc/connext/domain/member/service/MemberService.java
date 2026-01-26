package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public void checkUsernameDuplicate(String email) {
        if (memberRepository.existsByUsername(email)) {
            throw new GeneralException(ErrorCode.ID_ALREADY_EXISTS, "이미 가입된 이메일입니다.");
        }
    }

    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new GeneralException(ErrorCode.NICKNAME_ALREADY_EXISTS, "이미 사용 중인 닉네임입니다.");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일은 필수입니다.");
        }

        if (username.length() > 50) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일은 최대 50자까지 가능합니다.");
        }

        if (!EMAIL_PATTERN.matcher(username).matches()) {
            throw new GeneralException(ErrorCode.INVALID_MEMBER_ID ,"이메일 형식이 올바르지 않습니다.");
        }
    }
}
