package com.umc.connext.global.oauth2.handler;

import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.global.auth.enums.TokenCategory;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.auth.util.JWTProperties;
import com.umc.connext.global.auth.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.success}")
    private String successRedirectUri;
    @Value("${app.oauth2.signup}")
    private String signupRedirectUri;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final JWTProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Long memberId = customUserDetails.getMemberId();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        //약관 동의 전일 경우
        if (customUserDetails.getMemberStatus() == MemberStatus.PENDING) {
            String signupToken = jwtUtil.createJwt(TokenCategory.SIGNUP, null, memberId);

            response.addCookie(createCookie("signup", signupToken,
                    Math.toIntExact(jwtProperties.getSignupTokenValiditySeconds())));
            response.sendRedirect(signupRedirectUri);
            return;
        }

        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, role, memberId);

        //Refresh 토큰 저장
        refreshTokenService.saveRefreshToken(refresh, memberId);

        response.addCookie(createCookie("refresh", refresh,
                Math.toIntExact(jwtProperties.getRefreshTokenValiditySeconds())));
        response.sendRedirect(successRedirectUri);
    }

    private Cookie createCookie(String key, String value, int seconds) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(seconds);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}

