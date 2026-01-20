package com.umc.connext.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JwtTokenProvider jwtTokenProvider;

    public String getUsername(String token) {

        return jwtTokenProvider.getUsername(token);
    }

    public String getRole(String token) {

        return jwtTokenProvider.getRole(token);
    }

    public String getCategory(String token) {

        return jwtTokenProvider.getCategory(token);
    }

    public Boolean isExpired(String token) {

        return jwtTokenProvider.isExpired(token);
    }

    public String createJwt(String category, String username, String role) {

        return jwtTokenProvider.createJwt(category, username, role);
    }

    public void validateAccessToken(String token) {
        jwtTokenProvider.validateAccessToken(token);
    }

    public void validateRefreshToken(String token) {
        jwtTokenProvider.validateRefreshToken(token);
    }

    public boolean isRefreshToken(String token) {
        try {
            return jwtTokenProvider.isRefreshToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return jwtTokenProvider.isAccessToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}