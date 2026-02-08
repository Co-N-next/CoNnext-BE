package com.umc.connext.domain.mate.repository;

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
}
