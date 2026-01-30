package com.umc.connext.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(    description = "수정 요청 약관 목록 DTO",
        example = "{\n" +
                "  \"agreements\": [\n" +
                "    {\"termId\": 1, \"agreed\": true},\n" +
                "    {\"termId\": 2, \"agreed\": false},\n" +
                "    {\"termId\": 3, \"agreed\": true}\n" +
                "  ]\n" +
                "}")
public class OptionalTermsChangeDTO {

    @NotEmpty
    @Schema(description = "수정할 선택 약관 리스트")
    private List<TermAgreement> agreements;

    @Getter
    @NoArgsConstructor
    public static class TermAgreement {

        @NotNull
        @Schema(description = "약관 고유 ID", example = "2")
        private Long termId;

        @NotNull
        @Schema(description = "동의 여부 (true: 동의, false: 철회)", example = "true")
        private Boolean agreed;
    }
}
