package com.umc.connext.domain.reservation.repository;

import com.umc.connext.domain.mate.projection.MateReservationProjection;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.dto.ReservationGetResDTO;
import com.umc.connext.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByMemberIdAndConcertDetailIdAndFloorAndSectionAndRowAndSeat(
            Long memberId,
            Long concertDetailId,
            Integer floor,
            String section,
            String row,
            Integer seat
    );

    @Query("""
        SELECT DISTINCT new com.umc.connext.domain.reservation.dto.ReservationGetResDTO(
            r.id,
            c.name,
            COALESCE(cast.name, '미정'),
            cd.startAt,
            COALESCE(v.name, '미정'),
            new com.umc.connext.domain.reservation.dto.SeatInfoDTO(
                r.floor,
                r.section,
                r.row,
                r.seat
            )
        ) FROM Reservation r
        JOIN r.concertDetail cd
        JOIN cd.concert c
        LEFT JOIN c.concertCasts cc
        LEFT JOIN cc.cast cast
        LEFT JOIN c.concertVenues cv
        LEFT JOIN cv.venue v
        WHERE r.member = :member
        ORDER BY r.id DESC
    """)
    List<ReservationGetResDTO> findAllByMember(Member member);

    Optional<Reservation> findFirstByMemberIdAndConcertDetail_StartAtBetweenOrderByConcertDetail_StartAtAsc(Long memberId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("""
        SELECT
            c.id              AS concertId,
            c.name            AS concertName,
            c.posterImage     AS concertPosterImage,
            COALESCE(cast.name, '미정')         AS concertArtist,
            cd.startAt        AS startAt,
            COALESCE(v.name, '미정')            AS concertVenue,
            r.floor           AS floor,
            r.section         AS section,
            r.row             AS row,
            r.seat            AS seat
        FROM Reservation r
            JOIN r.concertDetail cd
            JOIN cd.concert c
            LEFT JOIN c.concertCasts cc
            LEFT JOIN cc.cast cast
            LEFT JOIN c.concertVenues cv
            LEFT JOIN cv.venue v
        WHERE r.member.id = :memberId
        ORDER BY cd.startAt DESC
    """)
    List<MateReservationProjection> findReservationSummariesByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.concertDetail cd
        JOIN FETCH cd.concert c
        WHERE r.member.id = :memberId
            AND CAST(cd.startAt AS date) = CAST(:today AS date)
        ORDER BY cd.startAt ASC
    """)
    List<Reservation> findMyTodayReservations(@Param("memberId") Long memberId, @Param("today") LocalDateTime today);

    // 진단용 단순 쿼리 - cast나 venue 없어도 작동
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.concertDetail cd
        JOIN FETCH cd.concert c
        WHERE r.member.id = :memberId
            AND YEAR(cd.startAt) = YEAR(CURRENT_DATE)
            AND MONTH(cd.startAt) = MONTH(CURRENT_DATE)
            AND DAY(cd.startAt) = DAY(CURRENT_DATE)
        ORDER BY cd.startAt ASC
    """)
    List<Reservation> findMyTodayReservationsSimple(@Param("memberId") Long memberId);

    // 네이티브 쿼리로 정확한 데이터 확인
    @Query(value = """
        SELECT COUNT(*) 
        FROM reservations r
        JOIN concert_details cd ON r.concert_detail_id = cd.id
        WHERE r.member_id = :memberId
            AND DATE(cd.start_at) = CURDATE()
    """, nativeQuery = true)
    int countTodayReservations(@Param("memberId") Long memberId);
}
