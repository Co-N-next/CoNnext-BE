package com.umc.connext.global.oauth2.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.dto.MemberDTO;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.global.oauth2.entity.CustomOAuth2User;
import com.umc.connext.global.oauth2.dto.GoogleResponse;
import com.umc.connext.global.oauth2.dto.KakaoResponse;
import com.umc.connext.global.oauth2.dto.NaverResponse;
import com.umc.connext.global.oauth2.dto.OAuth2Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("kakao")) {

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

        // 1️⃣ 소셜 계정 자체 존재 여부
        Member existMember = memberRepository.findByUsername(username).orElse(null);

        // 2️⃣ 자체 로그인 계정(email 기반)과 충돌 체크
        Member emailConflictMember = memberRepository.findByUsername(email).orElse(null);
        if (emailConflictMember != null && (existMember == null || emailConflictMember.getId() != existMember.getId())) {
            OAuth2Error error = new OAuth2Error(
                    "email_conflict",
                    ErrorCode.EMAIL_ALREADY_USED_BY_LOCAL.getMessage(), // 여기서 메시지 가져오기
                    ""
            );
            throw new OAuth2AuthenticationException(error);
        }

        if(existMember == null) {

            Member member = Member.createMember(username, email, null,oAuth2Response.getName(),Role.ROLE_USER);

            memberRepository.save(member);

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setUsername(username);
            memberDTO.setName(oAuth2Response.getName());
            memberDTO.setRole(Role.ROLE_USER);

            return new CustomOAuth2User(memberDTO);
        }
        else {
            //existMember.setEmail(oAuth2Response.getEmail());
            existMember.updateNickname(oAuth2Response.getName());

            memberRepository.save(existMember);

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setUsername(existMember.getUsername());
            memberDTO.setName(oAuth2Response.getName());
            memberDTO.setRole(existMember.getRole());

            return new CustomOAuth2User(memberDTO);
        }
    }
}

