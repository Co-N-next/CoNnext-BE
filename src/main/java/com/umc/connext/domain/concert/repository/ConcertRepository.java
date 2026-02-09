package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findTop10ByOrderByCreatedAtDesc();

    Page<Concert> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 다가오는 공연 목록 조회 (최신순)
     */
    @Query("""
        SELECT DISTINCT c
        FROM Concert c
        JOIN c.concertDetails cd
        WHERE cd.startAt >= :now
        """)
    Page<Concert> findUpcomingConcertsOrderByCreated(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 다가오는 공연 목록 조회 (조회수순)
     */
    @Query("""
        SELECT DISTINCT c
        FROM Concert c
        JOIN c.concertDetails cd
        WHERE cd.startAt >= :now
        """)
    Page<Concert> findUpcomingConcertsOrderByViewCount(@Param("now") LocalDateTime now, Pageable pageable);
}