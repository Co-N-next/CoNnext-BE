package com.umc.connext.domain.member.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.global.oauth2.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Entity
@Table(name = "member",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"socialType", "providerId"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false)
    private String providerId;

    @Column
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    @Column(length = 255)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'ROLE_USER'")
    private Role role;

    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private MemberNotificationSetting notificationSetting;

    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private MemberVisibilitySetting visibilitySetting;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MemberTerm> memberTerms = new ArrayList<>();

    public void updateMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public void restore() {
        super.restore();
        this.memberStatus = MemberStatus.ACTIVE;
    }
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImage = imageUrl;
    }

    public void softDelete() {
        this.memberStatus = MemberStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public static Member local(
            String email,
            String encodedPassword,
            String nickname
    ) {
        return Member.builder()
                .socialType(SocialType.LOCAL)
                .providerId(email)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .memberStatus(MemberStatus.ACTIVE)
                .build();
    }

    public static Member social(
            SocialType socialType,
            String providerId,
            String email,
            String nickname
    ) {
        return Member.builder()
                .socialType(socialType)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .memberStatus(MemberStatus.PENDING)
                .build();
    }
}
