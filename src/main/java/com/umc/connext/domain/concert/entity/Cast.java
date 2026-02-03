package com.umc.connext.domain.concert.entity;

import com.umc.connext.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "casts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cast extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String nickname;

    private String description;

    private String profileImg;

    @OneToMany(mappedBy = "cast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcertCast> concertCasts = new ArrayList<>();
}