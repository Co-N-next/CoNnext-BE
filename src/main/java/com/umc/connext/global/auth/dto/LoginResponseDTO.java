package com.umc.connext.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public record LoginResponseDTO(
        @Schema(description = "아이디 (username)", example = "qwer@example.com")
        String username
) {
}
