package com.umc.connext.domain.concert.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concerts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Concert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String posterImage;

    private String ageRating;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String price;

    private String reservationLink;

    @Builder.Default
    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcertCast> concertCasts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcertDetail> concertDetails = new ArrayList<>();
}