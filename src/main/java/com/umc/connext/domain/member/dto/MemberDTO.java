package com.umc.connext.domain.member.dto;

import com.umc.connext.common.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private Role role;
    private String name;
    private String username;
}