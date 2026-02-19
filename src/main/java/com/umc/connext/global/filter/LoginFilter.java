package com.umc.connext.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.global.auth.dto.LoginRequestDTO;
import com.umc.connext.global.auth.dto.LoginResponseDTO;
import com.umc.connext.global.auth.enums.TokenCategory;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.auth.util.JWTProperties;
import com.umc.connext.global.auth.util.JWTUtil;
import com.umc.connext.global.auth.util.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SecurityResponseWriter securityResponseWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JWTProperties jwtProperties;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JWTUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       SecurityResponseWriter securityResponseWriter,
                       JWTProperties jwtProperties) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.securityResponseWriter = securityResponseWriter;
        this.jwtProperties = jwtProperties;

        setFilterProcessesUrl("/auth/login/local");
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            // JSON body 읽기
            LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse login request body", e);
        }
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long memberId = userDetails.getMemberId();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();


        //토큰 생성
        String access = jwtUtil.createJwt(TokenCategory.ACCESS, role, memberId);
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, role, memberId);

        //기존 Refresh 토큰 제거
        refreshTokenService.removeAllByAuthKey(memberId);

        //Refresh 토큰 저장
        refreshTokenService.saveRefreshToken(refresh, memberId);

        //응답 설정
        response.setHeader("Authorization", "Bearer " + access);
        response.addCookie(createCookie("refresh", refresh));

        //ID(email) 와 닉네임 리턴
        LoginResponseDTO loginResponseDto = LoginResponseDTO.of(userDetails.getUsername(), userDetails.getNickname());
        Response<LoginResponseDTO> body = Response.success(SuccessCode.LOGIN_SUCCESS,loginResponseDto);

        securityResponseWriter.write(response, body);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, AuthenticationException {
        Throwable cause = failed.getCause();

        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

        if (cause instanceof DisabledException) {
            errorCode = ErrorCode.MEMBER_DELETED;
        }

        Response<Void> body =  Response.fail(errorCode);

        securityResponseWriter.write(response, body);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(Math.toIntExact(jwtProperties.getRefreshTokenValiditySeconds()));
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
