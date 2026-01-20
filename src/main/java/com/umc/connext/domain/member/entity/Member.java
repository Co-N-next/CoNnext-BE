package com.umc.connext.domain.member.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLDelete(sql = "UPDATE member SET deleted_at = NOW(), status = 'DELETED', is_delete = true WHERE id = ?")
@SQLRestriction("is_delete = false")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberStatus status; // ACTIVE, DELETED

    private boolean is_delete;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createMember(String username, String email, String password, String nickname, Role role) {
        Member member = new Member();
        member.username = username;
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.role = role;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

}
