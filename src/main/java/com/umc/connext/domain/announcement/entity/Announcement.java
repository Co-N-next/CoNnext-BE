package com.umc.connext.domain.announcement.entity;

import jakarta.persistence.*;
import com.umc.connext.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="announcements")
public class Announcement extends BaseEntity {

    @Id
    @Column(name = "announcement_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="sender_id", nullable=false)
    private Long senderId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", nullable=false)
    private String content;

    @Column(name="logo_img")
    private String logoImg;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<AnnouncementReadStatus> readStatuses = new ArrayList<>();
}
