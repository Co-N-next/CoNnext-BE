package com.umc.connext.domain.mate.repository;

import com.umc.connext.domain.mate.dto.TodayMateResDTO;
import com.umc.connext.domain.mate.entity.Mate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MateRepository extends JpaRepository<Mate, Long> {

    @Query("""
           SELECT m FROM Mate m
           WHERE (m.requester.id = :a AND m.addressee.id = :b)
              OR (m.requester.id = :b AND m.addressee.id = :a)
           """)
    Optional<Mate> findBetween(@Param("a") Long a, @Param("b") Long b);

    @Query("""
        SELECT m FROM Mate m
        WHERE (m.requester.id = :memberId OR m.addressee.id = :memberId)
          AND m.status = 'ACCEPTED'
    """)
    List<Mate> findAllAcceptedMatesByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT m FROM Mate m
        WHERE (m.requester.id = :me AND m.addressee.id IN :others)
            OR (m.requester.id IN :others AND m.addressee.id = :me)
    """)
    List<Mate> findRelationsWith(
            @Param("me") Long me,
            @Param("others") List<Long> others
    );

    @Query("""
        SELECT DISTINCT new com.umc.connext.domain.mate.dto.TodayMateResDTO(
            c.name as concertName,
            c.posterImage as concertPosterImage,
            m.id as mateId,
            partner.id as memberId,
            partner.nickname as nickname,
            partner.profileImage as profileImage,
            new com.umc.connext.domain.reservation.dto.SeatInfoDTO(
                r.floor,
                r.section,
                r.row,
                r.seat
            )
        )
        FROM Mate m
            JOIN Member partner ON (
                (m.requester.id = :memberId AND m.addressee.id = partner.id)
                OR (m.addressee.id = :memberId AND m.requester.id = partner.id)
            )
            JOIN Reservation r ON r.member.id = partner.id
            JOIN ConcertDetail cd ON cd.id = r.concertDetail.id
            JOIN cd.concert c
        WHERE m.status = 'ACCEPTED'
          AND r.concertDetail.id = :concertDetailId
    """)
    List<TodayMateResDTO> findTodayMatesByMemberId(
            @Param("memberId") Long memberId,
            @Param("concertDetailId") Long concertDetailId
    );
}
