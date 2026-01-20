package com.umc.connext.domain.concert.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "concerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "poster_image")
    private String posterImage;

    @Column(name = "age_rating", nullable = false)
    private String ageRating;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private String price;

    @Column(name = "reservation_link")
    private String reservationLink;
}