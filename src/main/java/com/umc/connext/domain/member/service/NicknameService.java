package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameService {

    private final NicknameGenerator nicknameGenerator;
    private final MemberRepository memberRepository;

    public String generateRandomNickname() {
        for (int i = 0; i < 10; i++) {
            String nickname = nicknameGenerator.generate();
            if (!memberRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        throw new GeneralException(ErrorCode.NICKNAME_GENERATION_FAILED,"랜덤 닉네임 생성에 실패했습니다. 관리자에게 문의바랍니다.");
    }
}