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
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

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
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"");
        }

        try {
            extractClaims(token);
        } catch (ExpiredJwtException e) {
            throw new GeneralException(ErrorCode.TOKEN_EXPIRED,"");
        } catch (MalformedJwtException e) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
        } catch (UnsupportedJwtException e) {
            throw new GeneralException(ErrorCode.UNSUPPORTED_TOKEN,"");
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"");
        } catch (Exception e) {
            log.error("Unexpected JWT validation error", e);
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR,"");
        }
    }

    public void validateAccessToken(String token) {
        token = extractToken(token);
        parseToken(token);

        if (!isAccessToken(token)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN_CATEGORY,"");
        }
    }

    public void validateRefreshToken(String token) {
        parseToken(token);

        if (!isRefreshToken(token)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN_CATEGORY,"");
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
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
        }
    }

    public String getRole(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
        }
    }

    public String getCategory(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).get("category", String.class);
        } catch (Exception e) {
            log.error("Error extracting category", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
        }
    }

    public boolean isExpired(String token) {

        token = extractToken(token);
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error extracting expiration time", e);
            throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
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
