package com.umc.connext.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Schema(description = "랜덤닉네임 생성 DTO")
public class NicknameDTO {
    @Schema(description = "사용자 nickname", example = "조용한강아지34")
    String nickname;

    public static NicknameDTO of(String nickname) {
        return new NicknameDTO(nickname);
    }
}
