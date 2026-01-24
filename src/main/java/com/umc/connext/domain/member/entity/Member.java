package com.umc.connext.domain.member.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLDelete(sql = "UPDATE member SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(length = 255)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'ROLE_USER'")
    private Role role;


    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImage = imageUrl;
    }

    public static Member createMember(String username, String email, String password, String nickname, Role role) {
        Member member = new Member();
        member.username = username;
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.role = role;
        return member;
    }
}
