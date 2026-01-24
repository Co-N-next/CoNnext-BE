package com.umc.connext.domain.concert.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concert_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConcertDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [concert_id] ERD 상 N:1 관계의 주인 (FK 보유)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    // [start_at] 공연 일시 (DATETIME)
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // [running_time] 관람 시간 (분 단위 INT)
    @Column(name = "running_time", nullable = false)
    private Integer runningTime;

    // [intermission] 인터미션 (분 단위 INT)
    @Column(name = "intermission")
    private Integer intermission;

    // [round] 회차 (INT)
    @Column(name = "round", nullable = false)
    private Integer round;

}