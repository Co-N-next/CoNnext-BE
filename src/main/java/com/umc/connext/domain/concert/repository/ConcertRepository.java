package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findTop10ByOrderByCreatedAtDesc();

    Page<Concert> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
    SELECT DISTINCT c
    FROM Concert c
    JOIN c.concertDetails cd
    WHERE cd.startAt >= :now
    """)
    Page<Concert> findUpcomingConcerts(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Concert c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id);
}