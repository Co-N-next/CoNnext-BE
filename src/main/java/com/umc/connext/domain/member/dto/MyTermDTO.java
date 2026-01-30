package com.umc.connext.domain.member.dto;

import com.umc.connext.domain.member.entity.MemberTerm;
import com.umc.connext.domain.member.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "동의한 optional 약관 목록 DTO")
public class MyTermDTO {
    private Long termId;
    private String title;
    private TermType type;
    private boolean isAgreed;

    public static MyTermDTO from(MemberTerm memberTerm) {
        return new MyTermDTO(
                memberTerm.getTerm().getId(),
                memberTerm.getTerm().getTitle(),
                memberTerm.getTerm().getType(),
                memberTerm.isAgreed()
        );
    }
}
