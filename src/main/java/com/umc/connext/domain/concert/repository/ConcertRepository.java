package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findTop10ByOrderByCreatedAtDesc();

    Page<Concert> findByNameContainingIgnoreCase(String name, Pageable pageable);
}