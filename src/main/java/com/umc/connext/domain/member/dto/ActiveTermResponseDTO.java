package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.entity.Term;
import com.umc.connext.domain.member.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "약관 목록 DTO")
public class ActiveTermResponseDTO {
    private Long id;
    private String title;
    private TermType type;

    public static ActiveTermResponseDTO from(Term term) {
        return new ActiveTermResponseDTO(term.getId(), term.getTitle(), term.getType());
    }
}
