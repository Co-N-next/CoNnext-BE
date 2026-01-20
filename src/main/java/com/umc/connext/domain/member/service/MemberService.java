package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void checkUsernameDuplicate(String email) {
        if (memberRepository.existsByUsername(email)) {
            throw new GeneralException(ErrorCode.ID_ALREADY_EXISTS, "");
        }
    }

    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new GeneralException(ErrorCode.NICKNAME_ALREADY_EXISTS, "");
        }
    }
}
