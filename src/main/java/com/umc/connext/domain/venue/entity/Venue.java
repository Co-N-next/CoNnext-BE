package com.umc.connext.domain.venue.entity;

import com.umc.connext.domain.venue.enums.Type;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venues")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Type type = Type.CONCERT_HALL;

    @Column(name = "total_views", nullable = false)
    private Integer totalViews;
}
