package com.umc.connext.global.filter;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.auth.util.JWTUtil;
import com.umc.connext.global.auth.util.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {

    private static final String LOGOUT_URI = "/auth/logout";
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SecurityResponseWriter securityResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain filterChain)
            throws ServletException, IOException {
        //path and method verify
        if (!isLogoutRequest(httpRequest)) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        try {
            String refreshToken = extractRefreshToken(httpRequest);

            jwtUtil.validateRefreshToken(refreshToken);

            refreshTokenService.removeRefreshToken(refreshToken);
            removeRefreshCookie(httpResponse);
            securityResponseWriter.write(
                    httpResponse,
                    Response.success(SuccessCode.LOGOUT_SUCCESS)
            );
        } catch (GeneralException e) {
            securityResponseWriter.write(
                    httpResponse,
                    Response.fail(e.getErrorCode())
            );
        }
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        return LOGOUT_URI.equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN, "쿠키가 존재하지 않습니다.");
        }

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"리프레시 토큰이 존재하지 않습니다.");
    }

    private void removeRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
