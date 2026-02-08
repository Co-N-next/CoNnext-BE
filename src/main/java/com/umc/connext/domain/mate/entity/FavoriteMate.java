package com.umc.connext.domain.mate.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_mate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FavoriteMate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id", nullable = false)
    private Mate mate;

    // 자주 찾는 메이트 추가
    public static FavoriteMate of(Member member, Mate mate) {
        return new FavoriteMate(null, member, mate);
    }
}
