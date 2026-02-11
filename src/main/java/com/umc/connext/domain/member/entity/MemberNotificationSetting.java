package com.umc.connext.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "member_notification_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = "member")
public class MemberNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_notification_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "service_notification_enabled", nullable = false)
    private Boolean serviceNotificationEnabled;

    @Column(name = "push_notification_enabled", nullable = false)
    private Boolean pushNotificationEnabled;

    @Column(name = "sms_notification_enabled", nullable = false)
    private Boolean smsNotificationEnabled;

    public static MemberNotificationSetting from(Member member) {
        return MemberNotificationSetting.builder()
                .member(member)
                .serviceNotificationEnabled(true) // 기본값: 켬
                .pushNotificationEnabled(false)   // 기본값: 끔
                .smsNotificationEnabled(false)    // 기본값: 끔
                .build();
    }

    public void update(boolean service, boolean push, boolean sms) {
        this.serviceNotificationEnabled = service;
        this.pushNotificationEnabled = push;
        this.smsNotificationEnabled = sms;
    }
}
