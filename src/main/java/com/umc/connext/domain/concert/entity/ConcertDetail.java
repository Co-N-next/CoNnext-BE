package com.umc.connext.domain.concert.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concert_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    private LocalDateTime startAt;

    private Integer runningTime;

    private Integer intermission;

    private Integer round;

    private Integer price;
}