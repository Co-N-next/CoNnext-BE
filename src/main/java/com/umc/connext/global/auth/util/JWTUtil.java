package com.umc.connext.global.auth.util;

import com.umc.connext.global.auth.enums.TokenCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JWTTokenProvider jwtTokenProvider;

    public String getRole(String token) {

        return jwtTokenProvider.getRole(token);
    }

    public Long getMemberId(String token) {
        return jwtTokenProvider.getMemberId(token);
    }

    public TokenCategory getCategory(String token) {
        return jwtTokenProvider.getCategory(token);
    }

    public String createJwt(TokenCategory category, String role, Long memberId) {
        return jwtTokenProvider.createJwt(category, role, memberId);
    }

    public void validateAccessToken(String token) {
        jwtTokenProvider.validateAccessToken(token);
    }

    public void validateRefreshToken(String token) {
        jwtTokenProvider.validateRefreshToken(token);
    }

    public void validateSignupToken(String token) {
        jwtTokenProvider.validateSignUpToken(token);
    }
}