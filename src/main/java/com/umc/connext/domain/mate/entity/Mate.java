package com.umc.connext.domain.mate.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.mate.enums.MateStatus;
import com.umc.connext.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mate")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Mate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private Member addressee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MateStatus status;

    // 친구 요청
    public static Mate request(Member requester, Member addressee) {
        Mate mate = new Mate();
        mate.requester = requester;
        mate.addressee = addressee;
        mate.status = MateStatus.PENDING;
        return mate;
    }

    // 친구 수락
    public void accept() {
        this.status = MateStatus.ACCEPTED;
    }

    // 친구 거절
    public void reject() {
        this.status = MateStatus.REJECTED;
    }
}
