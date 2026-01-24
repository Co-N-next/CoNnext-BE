package com.umc.connext.global.auth.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.global.auth.dto.ReissueResultDTO;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public ReissueResultDTO reissue(String refreshToken) {

        jwtUtil.validateRefreshToken(refreshToken);

        if (!refreshTokenService.existsByRefreshToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN, "");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role);
        String newRefresh = jwtUtil.createJwt("refresh", username, role);

        //Refresh 토큰 저장 기존 DB Refresh 제거 후 새 Refresh 저장함
        refreshTokenService.removeRefreshToken(refreshToken);
        refreshTokenService.saveRefreshToken(newRefresh, username);

        return new ReissueResultDTO(newAccess, newRefresh);
    }
}
