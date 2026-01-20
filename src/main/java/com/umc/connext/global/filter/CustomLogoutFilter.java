package com.umc.connext.global.filter;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.util.JWTUtil;
import com.umc.connext.global.util.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private static final String LOGOUT_URI = "/auth/logout";

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SecurityResponseWriter securityResponseWriter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //path and method verify
        if (!isLogoutRequest(httpRequest)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String refreshToken = extractRefreshToken(request);

            jwtUtil.validateRefreshToken(refreshToken);

            if (!refreshTokenService.existsByRefreshToken(refreshToken)) {
                throw new GeneralException(ErrorCode.INVALID_TOKEN,"");
            }

            refreshTokenService.removeRefreshToken(refreshToken);
            removeRefreshCookie(response);

            securityResponseWriter.write(
                    response,
                    Response.success(SuccessCode.LOGOUT_SUCCESS)
            );

        } catch (GeneralException e) {
            securityResponseWriter.write(
                    response,
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
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN,"");
    }

    private void removeRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        //cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
