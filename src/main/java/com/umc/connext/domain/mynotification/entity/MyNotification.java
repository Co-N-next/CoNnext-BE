package com.umc.connext.domain.mynotification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.umc.connext.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="my_notifications")
public class MyNotification extends BaseEntity {

    @Id
    @Column(name = "my_notification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myNotificationId;

    @Column(name="member_id", nullable=false)
    private Long memberId;

    @Column(name="sender_id", nullable=false)
    private Long senderId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", nullable=false)
    private String content;

    @Column(name="is_agreed", nullable=false)
    private Boolean agreed;

    @Enumerated(EnumType.STRING)
    @Column(name="category", nullable=false)
    private Category category;

}
