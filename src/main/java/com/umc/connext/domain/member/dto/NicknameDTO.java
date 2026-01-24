package com.umc.connext.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class NicknameDTO {
    String nickname;

    public static NicknameDTO of(String nickname) {
        return new NicknameDTO(nickname);
    }
}
