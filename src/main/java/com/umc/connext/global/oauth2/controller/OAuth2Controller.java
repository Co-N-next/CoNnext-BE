package com.umc.connext.global.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth2", description = "소셜 로그인 관련 API")
@RestController
public class OAuth2Controller {

    @Operation(
            summary = "소셜 로그인 시작 (리다이렉트)",
            description = """
        소셜 로그인 페이지로 이동합니다.
        로그인 성공 시 서버에서 인증을 처리한 후
        프론트엔드 지정 URL로 리다이렉트됩니다.
        
        - Refresh Token은 HttpOnly Cookie로 발급됩니다.
        - Access Token은 Reissue API 호출을 통해 발급해야합니다.
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
}
