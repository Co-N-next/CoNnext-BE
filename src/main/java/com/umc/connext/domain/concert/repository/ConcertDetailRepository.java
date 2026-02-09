package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
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

    /**
     * 특정 공연의 다음 공연 시간 조회 (현재 이후 가장 가까운 시간)
     */
    @Query("SELECT MIN(cd.startAt) FROM ConcertDetail cd WHERE cd.concert = :concert AND cd.startAt >= :now")
    LocalDateTime findNextShowTime(@Param("concert") Concert concert, @Param("now") LocalDateTime now);
}