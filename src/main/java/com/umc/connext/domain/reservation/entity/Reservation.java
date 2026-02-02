package com.umc.connext.domain.reservation.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_detail_id", nullable = false)
    private ConcertDetail concertDetail;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "section")
    private String section;

    @Column(name = "seat_row")
    private String row;

    @Column(name = "seat_number")
    private Integer seat;

}
