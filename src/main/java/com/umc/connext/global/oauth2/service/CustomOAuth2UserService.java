package com.umc.connext.global.oauth2.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.dto.MemberDTO;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.global.oauth2.principal.CustomOAuth2User;
import com.umc.connext.global.oauth2.dto.GoogleResponse;
import com.umc.connext.global.oauth2.dto.KakaoResponse;
import com.umc.connext.global.oauth2.dto.NaverResponse;
import com.umc.connext.global.oauth2.dto.OAuth2Response;
import com.umc.connext.global.oauth2.enums.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String OAUTH_DUMMY_PASSWORD = "OAUTH2_USER";


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
        if (registrationId.equals(OAuth2Provider.NAVER.getValue())) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals(OAuth2Provider.GOOGLE.getValue())) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals(OAuth2Provider.KAKAO.getValue())) {

            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {
            OAuth2Error error = new OAuth2Error(
                    "invalid_login_provider",
                    ErrorCode.INVALID_LOGIN_TYPE.getMessage(), // 여기서 메시지 가져오기
                    ""
            );
            throw new OAuth2AuthenticationException(error);
        }


        String username = oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String name = oAuth2Response.getName();

        // 1️ 소셜 계정 자체 존재 여부
        Optional<Member> existMember = memberRepository.findByUsername(username);

        // 2️ 자체 로그인 계정(email 기반)과 충돌 체크 - 일부로 email 넣음 -
        memberRepository.findByUsername(email)
                .ifPresent(conflictMember -> {
                    boolean isDifferentMember =
                            existMember
                                    .map(m -> !m.getId().equals(conflictMember.getId()))
                                    .orElse(true);

                    if (isDifferentMember) {
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error(
                                        "email_conflict",
                                        ErrorCode.EMAIL_ALREADY_USED_BY_LOCAL.getMessage(),
                                        ""
                                )
                        );
                    }
                });

        // 3️ 회원 없으면 생성, 있으면 그대로 사용
        Member member = existMember.orElseGet(() -> memberRepository.save( Member.of(
                username, email, passwordEncoder.encode(OAUTH_DUMMY_PASSWORD), name, Role.ROLE_USER )));

        // 4 DTO 변환
        MemberDTO memberDTO = MemberDTO.of(member);

        return new CustomOAuth2User(memberDTO);
    }
}

