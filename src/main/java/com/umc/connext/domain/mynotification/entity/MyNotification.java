package com.umc.connext.domain.mynotification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.mynotification.entity.ActionStatus;
import com.umc.connext.domain.mynotification.entity.ActionType;
import com.umc.connext.domain.mynotification.entity.Category;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="my_notifications")
public class MyNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myNotificationId;

    @Column(name="member_id", nullable=false)
    private Long memberId;

    @Column(name="sender_id")
    private Long senderId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", nullable=false)
    private String content;

    @Column(name="img")
    private String img;

    @Enumerated(EnumType.STRING)
    @Column(name="category", nullable=false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name="action_type", nullable=false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name="action_status", nullable=false)
    private ActionStatus actionStatus;

    @Column(name="is_read", nullable=false)
    private Boolean isRead;

    // 단순 알림
    public static MyNotification createNotice(
            Long memberId,
            String title,
            String content,
            String img
    ) {
        MyNotification n = new MyNotification();
        n.memberId = memberId;
        n.senderId = null;
        n.title = title;
        n.content = content;
        n.img = img;
        n.category = Category.NOTICE;
        n.actionType = ActionType.NONE;
        n.actionStatus = ActionStatus.PENDING;
        n.isRead = false;
        return n;
    }

    // 메이트 요청 및 위치 공유 요청
    public static MyNotification createSocial(
            Long memberId,
            Long senderId,
            String title,
            String content,
            String img,
            Category category,
            ActionType actionType
    ) {
        MyNotification n = new MyNotification();
        n.memberId = memberId;
        n.senderId = senderId;
        n.title = title;
        n.content = content;
        n.img = img;
        n.category = category;
        n.actionType = actionType;
        n.actionStatus = ActionStatus.PENDING;
        n.isRead = false;
        return n;
    }


    public void markAsRead(){
        this.isRead = true;
    }

    public void accept() {
        this.actionStatus = ActionStatus.ACCEPTED;
        this.isRead = true;
    }

    public void reject() {
        this.actionStatus = ActionStatus.REJECTED;
        this.isRead = true;
    }
}
