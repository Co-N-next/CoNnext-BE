package com.umc.connext.global.refreshtoken.service;

import com.umc.connext.global.refreshtoken.entity.RefreshToken;
import com.umc.connext.global.refreshtoken.repository.RefreshTokenRepository;
import com.umc.connext.global.util.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public void saveRefreshToken(String refreshToken, String authKey) {

        long ttlSeconds = jwtProperties.getRefreshTokenValidity() / 1000;

        RefreshToken token = RefreshToken.builder()
                .jwtRefreshToken(refreshToken)
                .authKey(authKey)
                .ttl(ttlSeconds)
                .build();
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void removeRefreshToken(String refreshToken) {
        refreshTokenRepository.findRefreshTokenByJwtRefreshToken(refreshToken)
                .ifPresent(token -> refreshTokenRepository.delete(token));
    }

    public boolean existsByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findRefreshTokenByJwtRefreshToken(refreshToken).isPresent();
    }

    @Transactional
    public void removeAllByAuthKey(String authKey) {
        refreshTokenRepository.deleteAllByAuthKey(authKey);
    }
}
