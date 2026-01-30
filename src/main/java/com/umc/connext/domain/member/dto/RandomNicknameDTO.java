package com.umc.connext.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "랜덤닉네임 생성 DTO")
public class RandomNicknameDTO {
    @Schema(description = "사용자 nickname", example = "조용한강아지34")
    String nickname;

    public static RandomNicknameDTO of(String nickname) {
        return new RandomNicknameDTO(nickname);
    }
}
