package com.umc.connext.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Social 회원가입 요청 DTO")
public class JoinSocialRequestDTO {

    @Schema(description = "동의한 약관 ID 리스트", example = "[1, 2, 4]")
    private List<Long> agreedTermIds;
}
