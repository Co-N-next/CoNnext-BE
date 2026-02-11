package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConcertDetailRepository extends JpaRepository<ConcertDetail, Long> {

    // N+1 문제 방지를 위해 Concert 정보도 한 번에 가져오기 (Fetch Join)
    @Query("SELECT cd FROM ConcertDetail cd JOIN FETCH cd.concert WHERE cd.id = :id")
    Optional<ConcertDetail> findByIdWithConcert(@Param("id") Long id);

    List<ConcertDetail> findAllByConcertOrderByStartAtAsc(Concert concert);

    List<ConcertDetail> findByStartAtBetween(LocalDateTime start, LocalDateTime end);

    List<ConcertDetail> findTop20ByStartAtAfterOrderByStartAtAsc(LocalDateTime now);
}