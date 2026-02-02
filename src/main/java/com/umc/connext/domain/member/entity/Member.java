package com.umc.connext.domain.member.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

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
}