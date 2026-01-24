package com.umc.connext.domain.member.dto;

import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private Role role;
    private String name;
    private String username;

    public static MemberDTO of(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.username = member.getUsername();
        dto.name = member.getNickname();
        dto.role = member.getRole();
        return dto;
    }
}