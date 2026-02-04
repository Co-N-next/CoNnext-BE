package com.umc.connext.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "로그인 응답 DTO")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponseDTO {

        @Schema(description = "아이디 (email)", example = "qwer@example.com")
        private String email;

        public static LoginResponseDTO of(String email) {
                return LoginResponseDTO.builder()
                        .email(email)
                        .build();
        }
}
