package com.umc.connext.domain.concert.repository;

import com.umc.connext.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

}