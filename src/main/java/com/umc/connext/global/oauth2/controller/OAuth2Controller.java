package com.umc.connext.global.oauth2.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.global.auth.dto.JoinSocialRequestDTO;
import com.umc.connext.global.auth.enums.TokenCategory;
import com.umc.connext.global.auth.service.AuthService;
import com.umc.connext.global.refreshtoken.service.RefreshTokenService;
import com.umc.connext.global.auth.util.JWTProperties;
import com.umc.connext.global.auth.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OAuth2", description = "소셜 로그인 관련 API")
@RestController
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;
    private final JWTProperties jwtProperties;

    @Operation(
            summary = "소셜 로그인 시작 (리다이렉트)",
            description = """
        소셜 로그인 페이지로 이동합니다.
        로그인 성공 시 서버에서 인증을 처리한 후
        프론트엔드 지정 URL로 리다이렉트됩니다.
        
        - Refresh Token은 HttpOnly Cookie로 발급됩니다.
        - Access Token은 Reissue API 호출을 통해 발급해야합니다.
        - 신규 로그인시에는 Signup Token이 HttpOnly Cookie로 발급됩니다. /auth/signup/social 에서 해당 토큰을 사용합니다.
        - 본 API는 브라우저 리다이렉트를 전제로 하며 Swagger에서 직접 실행해도 정상 동작하지 않습니다.
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "소셜 로그인 성공 → 프론트엔드로 리다이렉트",
                            headers = {

                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Refresh Token (HttpOnly)",
                                            schema = @Schema(example = "refresh=eyJhbGciOiJIUzI1NiJ9...; HttpOnly; Path=/"))
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "지원하지 않는 소셜 로그인 타입"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이메일 충돌 (이미 로컬 계정 존재)"
                    )
            },
            parameters = {
                    @Parameter(
                            name = "registrationId",
                            description = "소셜 서비스 결정 (google, naver, kakao)",
                            required = true,
                            example = "google"
                    )
            }
    )
    @GetMapping("/oauth2/authorization/{registrationId}")
    public void socialLogin(@PathVariable String registrationId) {
        throw new IllegalStateException("OAuth2 필터에서 처리되는 엔드포인트입니다.");
    }

    @Operation(
            summary = "Social 회원가입",
            description = "소셜로그인이 처음일 경우 약관동의 목록을 받아 회원가입을 진행합니다.\n" +
                    "- 회원가입 토큰(쿠키)이 필요하며 이는 소셜 최초 로그인시에 발급됩니다.\n" +
                    "- Refresh Token은 HttpOnly Cookie로 발급됩니다.\n"+
                    "- Access Token은 Reissue API 호출을 통해 발급해야합니다.\n",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
            }
    )
    @PostMapping("/auth/signup/social")
    public ResponseEntity<Response<Void>> joinSocial(@CookieValue(value = "signup", required = false) String signupToken,
                                                    @RequestBody @Valid JoinSocialRequestDTO joinSocialRequestDTO,
                                                     HttpServletResponse response){

        if (signupToken == null) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TOKEN, "회원가입 토큰이 없습니다.");
        }

        jwtUtil.validateSignupToken(signupToken);
        Long memberId = jwtUtil.getMemberId(signupToken);
        Member member = authService.joinSocial(memberId, joinSocialRequestDTO);
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, member.getRole().name(), member.getId());

        refreshTokenService.saveRefreshToken(refresh, member.getId());
        response.addCookie(createCookie("refresh", refresh));
        response.addCookie(expireCookie("signup"));

        return ResponseEntity
                .status(SuccessCode.JOIN_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.JOIN_SUCCESS));
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(Math.toIntExact(jwtProperties.getRefreshTokenValiditySeconds()));
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private Cookie expireCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
