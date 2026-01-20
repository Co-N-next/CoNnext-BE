package com.umc.connext.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissueResultDto {
    String accessToken;
    String refreshToken;
}
