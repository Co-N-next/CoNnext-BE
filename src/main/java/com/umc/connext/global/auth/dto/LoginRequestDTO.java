package com.umc.connext.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDTO(
        @Schema(description = "아이디 (username)", example = "qwer@example.com")
        String username,

        @Schema(description = "비밀번호 (password)", example = "qwer1234")
        String password
) {

}
