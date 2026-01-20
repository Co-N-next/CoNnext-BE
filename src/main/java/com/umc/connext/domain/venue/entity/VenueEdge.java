package com.umc.connext.domain.venue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venue_edges", indexes = {
        @Index(name = "idx_node_from", columnList = "node_from"),
        @Index(name = "idx_node_to", columnList = "node_to"),
        @Index(name = "idx_venue_edge", columnList = "venue_id, node_from, node_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "node_from", nullable = false)
    private Long nodeFrom;

    @Column(name = "node_to", nullable = false)
    private Long nodeTo;

    @Column(name = "weight", precision = 10, scale = 2, nullable = false)
    private BigDecimal weight; // 거리(가중치)

    /**
     * 양방향 엣지인지 확인 (from과 to를 바꿔서 비교)
     */
    public boolean connects(Long node1, Long node2) {
        return (nodeFrom.equals(node1) && nodeTo.equals(node2)) ||
                (nodeFrom.equals(node2) && nodeTo.equals(node1));
    }
}
