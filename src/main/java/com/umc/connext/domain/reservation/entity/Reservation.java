package com.umc.connext.domain.reservation.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_reservation_member_concert_seat",
                        columnNames = {"member_id", "concert_detail_id", "floor", "section", "seat_row", "seat_number"}
                )
        }
)
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

    public void changeConcertDetail(ConcertDetail concertDetail) {
        this.concertDetail = concertDetail;
    }

    public void changeSeatInfo(Integer floor, String section, String row, Integer seat) {
        this.floor = floor;
        this.section = section;
        this.row = row;
        this.seat = seat;
    }
}
