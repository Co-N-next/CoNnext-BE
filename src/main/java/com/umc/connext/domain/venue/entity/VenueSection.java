package com.umc.connext.domain.venue.entity;

import com.umc.connext.domain.venue.dto.Coordinate;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;


@Entity
@Table(name = "venue_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "section_id", length = 50)
    private String sectionId;

    @Column(name = "full_path", columnDefinition = "TEXT")
    private String fullPath;

    @Column(name = "vertices", columnDefinition = "JSON")
    private String vertices; // JSON 형식: [[x1, y1], [x2, y2], ...]

    @Column(name = "center_x", precision = 10, scale = 1)
    private BigDecimal centerX;

    @Column(name = "center_y", precision = 10, scale = 1)
    private BigDecimal centerY;

    /**
     * JSON vertices를 좌표 리스트로 변환
     */
    @Transient
    public List<Coordinate> getVerticesList() {
        if (vertices == null || vertices.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(vertices, new TypeReference<List<Coordinate>>(){});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 좌표 리스트를 JSON으로 변환
     */
    public void setVerticesList(List<Coordinate> coordinateList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.vertices = mapper.writeValueAsString(coordinateList);
        } catch (Exception e) {
            this.vertices = "[]";
        }
    }
}
