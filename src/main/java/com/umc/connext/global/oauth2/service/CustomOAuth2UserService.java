package com.umc.connext.global.oauth2.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.service.NicknameService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import com.umc.connext.global.oauth2.dto.GoogleResponse;
import com.umc.connext.global.oauth2.dto.KakaoResponse;
import com.umc.connext.global.oauth2.dto.NaverResponse;
import com.umc.connext.global.oauth2.dto.OAuth2Response;
import com.umc.connext.global.oauth2.enums.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final NicknameService nicknameService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
        if (registrationId.equals(SocialType.NAVER.getValue())) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals(SocialType.GOOGLE.getValue())) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals(SocialType.KAKAO.getValue())) {

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

        String email = oAuth2Response.getEmail();
        String providerId = oAuth2Response.getProviderId();
        SocialType socialType = oAuth2Response.getProvider();
        String nickname = oAuth2Response.getName();

        //소셜 계정 자체 존재 여부
        Optional<Member> socialMember = memberRepository.findIncludingDeletedBySocialTypeAndProviderId(
                socialType, providerId);

        if (socialMember.isPresent()) {
            Member member = socialMember.get();

            if (member.getMemberStatus() == MemberStatus.DELETED) {
                member.restore();
                memberRepository.save(member);
            }
            return new CustomUserDetails(socialMember.get());
        }

        //자체 로그인(email)과 충돌 체크
        boolean localEmailExists = memberRepository.existsByEmailAndSocialType(email, SocialType.LOCAL);

        if (localEmailExists) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "email_conflict",
                            ErrorCode.EMAIL_ALREADY_USED_BY_LOCAL.getMessage(),
                            ""
                    )
            );
        }


        //닉네임 중복 처리
        if (nickname == null || memberRepository.existsByNickname(nickname)) {
            nickname = nicknameService.generateRandomNickname();
        }

        //신규 소셜 회원 생성
        Member member = memberRepository.save(
                Member.social(
                        socialType,
                        providerId,
                        email,
                        nickname
                )
        );

        return new CustomUserDetails(member);
    }
}
