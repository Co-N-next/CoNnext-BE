package com.umc.connext.global.util;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
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

    public String createJwt(String category ,String username, String role) {

        Date now = new Date();

        long validity = "access".equalsIgnoreCase(category)
                ? jwtProperties.getAccessTokenValidity()
                : jwtProperties.getRefreshTokenValidity();

        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .signWith(jwtProperties.getSecretKey())
                .compact();
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

    public String getUsername(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("username", String.class);
        } catch (Exception e) {
            log.error("Error extracting username", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        }
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

    public String getCategory(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("category", String.class);
        } catch (Exception e) {
            log.error("Error extracting category", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        }
    }

    public boolean isExpired(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error extracting expiration time", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"유효하지 않은 토큰입니다.");
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equalsIgnoreCase(getCategory(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equalsIgnoreCase(getCategory(token));
        } catch (Exception e) {
            return false;
        }
    }
}
