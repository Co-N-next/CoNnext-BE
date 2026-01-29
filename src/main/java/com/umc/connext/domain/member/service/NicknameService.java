package com.umc.connext.domain.member.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void changeNickname(Long memberId, String newNickname) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_MEMBER_FOUND, "존재하지 않는 회원입니다."));

        checkNicknameDuplicate(newNickname);
        member.updateNickname(newNickname);
    }

    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new GeneralException(ErrorCode.NICKNAME_ALREADY_EXISTS, "이미 사용 중인 닉네임입니다.");
        }
    }
}