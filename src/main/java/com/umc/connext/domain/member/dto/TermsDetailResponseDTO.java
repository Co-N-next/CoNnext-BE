package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.entity.Term;
import com.umc.connext.domain.member.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "약관 상세정보 조회 DTO")
public class TermsDetailResponseDTO {

    private Long id;
    private String title;
    private TermType type;
    private String content;

    public static TermsDetailResponseDTO from(Term term) {
        return new TermsDetailResponseDTO(
                term.getId(),
                term.getTitle(),
                term.getType(),
                term.getContent()
        );
    }
}
