package com.umc.connext.domain.concert.entity;

import com.umc.connext.common.entity.BaseEntity;
import com.umc.connext.domain.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "concert_venue")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConcertVenue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
}