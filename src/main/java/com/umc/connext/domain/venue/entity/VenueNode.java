package com.umc.connext.domain.venue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venue_nodes", indexes = {
        @Index(name = "idx_venue_node", columnList = "venue_id, node_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueNode {

    @Id
    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "x", precision = 10, scale = 1, nullable = false)
    private BigDecimal x;

    @Column(name = "y", precision = 10, scale = 1, nullable = false)
    private BigDecimal y;

    /**
     * 두 노드 사이의 유클리드 거리 계산
     */
    public double distanceTo(VenueNode other) {
        double dx = this.x.doubleValue() - other.x.doubleValue();
        double dy = this.y.doubleValue() - other.y.doubleValue();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
