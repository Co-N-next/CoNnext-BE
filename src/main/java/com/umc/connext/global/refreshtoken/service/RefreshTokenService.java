package com.umc.connext.global.refreshtoken.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.global.refreshtoken.entity.RefreshToken;
import com.umc.connext.global.refreshtoken.repository.RefreshTokenRepository;
import com.umc.connext.global.auth.util.JWTProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProperties jwtProperties;

    @Transactional
    public void saveRefreshToken(String refreshToken, Long authKey) {

        try {
            RefreshToken token = RefreshToken.builder()
                    .refreshToken(refreshToken)
                    .authKey(authKey)
                    .ttl(jwtProperties.getRefreshTokenValiditySeconds())
                    .build();

            refreshTokenRepository.save(token);

        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(
                    ErrorCode.INVALID_TOKEN,
                    "리프레시 토큰 저장에 실패했습니다."
            );
        }
    }

    @Transactional
    public void removeRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }

    public boolean existsById(String refreshToken) {
        return refreshTokenRepository.existsById(refreshToken);
    }

    @Transactional
    public void removeAllByAuthKey(Long authKey) {
        refreshTokenRepository.deleteAllByAuthKey(authKey);
    }
}
