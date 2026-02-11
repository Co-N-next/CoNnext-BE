package com.umc.connext.domain.announcement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.umc.connext.domain.member.entity.Member;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "announcement_read_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_announcement_member",
                        columnNames = {"announcement_id", "member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static AnnouncementReadStatus create(Announcement announcement, Member member) {
        AnnouncementReadStatus status = new AnnouncementReadStatus();
        status.announcement = announcement;
        status.member = member;
        status.isRead = false;
        return status;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
