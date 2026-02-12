package com.umc.connext.domain.mate.dto;

import jakarta.validation.constraints.NotNull;

public class MateReqDTO {

    public record MateRequestDTO(
            @NotNull(message = "addresseeId는 필수입니다.") Long addresseeId
    ){}

}
