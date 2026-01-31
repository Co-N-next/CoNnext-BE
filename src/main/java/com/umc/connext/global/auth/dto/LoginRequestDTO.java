package com.umc.connext.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDTO(
        @Schema(description = "아이디 (email)", example = "qwer@example.com")
        String email,

        @Schema(description = "비밀번호 (password)", example = "qwer1234")
        String password
) {

}
