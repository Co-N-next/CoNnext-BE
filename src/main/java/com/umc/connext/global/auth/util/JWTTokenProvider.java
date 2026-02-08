package com.umc.connext.global.auth.util;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.global.auth.enums.TokenCategory;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTTokenProvider {

    private final JWTProperties jwtProperties;

    public String createJwt(TokenCategory category, String role, Long memberId) {
        Date now = new Date();
        long validity = switch (category) {
            case ACCESS -> jwtProperties.getAccessTokenValidity();
            case REFRESH -> jwtProperties.getRefreshTokenValidity();
            case SIGNUP -> jwtProperties.getSignupTokenValidity();
        };

        Date expiryDate = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .claim("category", category.getValue())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .signWith(jwtProperties.getSecretKey());

        if (category != TokenCategory.SIGNUP) {
            builder.claim("role", role);
        }

        builder.claim("memberId", memberId);

        return builder.compact();
    }


    public void parseToken(String token) {
        if (token == null  || token.trim().isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"토큰이 존재하지 않습니다.");
        }

        try {
            extractClaims(token);
        } catch (ExpiredJwtException e) {
            throw new GeneralException(ErrorCode.TOKEN_EXPIRED,"토큰이 만료되었습니다.");
        } catch (MalformedJwtException e) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new GeneralException(ErrorCode.UNSUPPORTED_TOKEN,"지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"토큰이 존재하지 않습니다.");
        } catch (Exception e) {
            log.error("Unexpected JWT validation error", e);
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR,"서버 에러, 관리자에게 문의 바랍니다.");
        }
    }

    public void validateAccessToken(String token) {
        token = extractToken(token);
        parseToken(token);

        if (!isAccessToken(token)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN_CATEGORY,"토큰 타입이 올바르지 않습니다.");
        }
    }

    public void validateRefreshToken(String token) {
        parseToken(token);

        if (!isRefreshToken(token)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN_CATEGORY,"토큰 타입이 올바르지 않습니다.");
        }
    }

    public void validateSignUpToken(String token) {
        parseToken(token);

        if (!isSignUpToken(token)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN_CATEGORY,"토큰 타입이 올바르지 않습니다.");
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtProperties.getSecretKey()) // setSigningKey 대신 verifyWith
                .build()
                .parseSignedClaims(token) // parseClaimsJws 대신 parseSignedClaims
                .getPayload(); // getBody 대신 getPayload
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    public String getRole(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        }
    }

    public TokenCategory getCategory(String token) {

        token = extractToken(token);
        try {
            String category = extractClaims(token).get("category", String.class);
            return TokenCategory.from(category);
        } catch (Exception e) {
            log.error("Error extracting category", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equalsIgnoreCase(getCategory(token).getValue());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equalsIgnoreCase(getCategory(token).getValue());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignUpToken(String token) {
        try {
            return "signup".equalsIgnoreCase(getCategory(token).getValue());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getMemberId(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("memberId", Long.class);
        } catch (Exception e) {
            throw new GeneralException(
                    ErrorCode.INVALID_TOKEN,
                    "유효하지 않은 토큰입니다."
            );
        }
    }
}
