package com.umc.connext.global.nickname.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.global.nickname.entity.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameService {

    private final NicknameGenerator nicknameGenerator;
    private final MemberRepository memberRepository;

    public String generateUniqueNickname() {
        for (int i = 0; i < 10; i++) {
            String nickname = nicknameGenerator.generate();
            if (!memberRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        throw new GeneralException(ErrorCode.NICKNAME_GENERATION_FAILED,"");
    }
}