package com.umc.connext.global.auth.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.global.auth.dto.ReissueResultDTO;
import com.umc.connext.global.auth.enums.TokenCategory;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.auth.util.JWTUtil;
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

        if (!refreshTokenService.existsById(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long memberId = jwtUtil.getMemberId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String newAccess = jwtUtil.createJwt(TokenCategory.ACCESS, role, memberId);
        String newRefresh = jwtUtil.createJwt(TokenCategory.REFRESH, role, memberId);

        //기존 Refresh 제거 후 새 Refresh 저장
        refreshTokenService.removeAllByAuthKey(memberId);
        refreshTokenService.saveRefreshToken(newRefresh, memberId);

        return new ReissueResultDTO(newAccess, newRefresh);
    }
}
