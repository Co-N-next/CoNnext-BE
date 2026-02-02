package com.umc.connext.global.jwt.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(
                        ErrorCode.NOT_FOUND_MEMBER,
                        "존재하지 않는 회원입니다."
                ));

        if (member.getMemberStatus() == MemberStatus.DELETED) {
            throw new DisabledException("삭제된 아이디입니다.");
        }
        return new CustomUserDetails(member);
    }
}
