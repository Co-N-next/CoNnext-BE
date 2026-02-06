package com.umc.connext.global.oauth2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Schema(description = "회원가입 info DTO")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class SignupInfoResponseDTO {

    @Schema(description = "아이디 (email)", example = "qwer@example.com")
    private String email;

    public static SignupInfoResponseDTO of(String email) {
        return SignupInfoResponseDTO.builder()
                .email(email)
                .build();
    }
}
