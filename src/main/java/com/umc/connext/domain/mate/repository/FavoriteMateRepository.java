package com.umc.connext.domain.mate.repository;

import com.umc.connext.domain.mate.entity.FavoriteMate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteMateRepository extends JpaRepository<FavoriteMate, Long> {
    boolean existsByMemberIdAndMateId(Long memberId, Long mateId);

    Optional<FavoriteMate> findByMemberIdAndMateId(Long memberId, Long mateId);

    List<FavoriteMate> findAllByMemberId(Long memberId);

    void deleteAllByMateId(Long mateId);
}
