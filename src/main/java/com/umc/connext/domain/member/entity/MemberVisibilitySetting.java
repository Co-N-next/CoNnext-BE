package com.umc.connext.domain.member.entity;

import com.umc.connext.domain.member.enums.PerformanceVisibility;
import com.umc.connext.domain.member.enums.SeatVisibility;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "member_visibility_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberVisibilitySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_visibility_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_visibility", nullable = false)
    private PerformanceVisibility performanceVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_visibility", nullable = false)
    private SeatVisibility seatVisibility;

    public static MemberVisibilitySetting from(Member member) {
        return MemberVisibilitySetting.builder()
                .member(member)
                .performanceVisibility(PerformanceVisibility.TODAY_ONLY)
                .seatVisibility(SeatVisibility.SECTION_ONLY)
                .build();
    }

    public void update(PerformanceVisibility performanceVisibility, SeatVisibility seatVisibility) {
        this.performanceVisibility = performanceVisibility;
        this.seatVisibility = seatVisibility;
    }
}
