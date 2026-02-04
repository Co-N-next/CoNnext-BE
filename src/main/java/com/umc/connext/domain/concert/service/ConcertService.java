package com.umc.connext.domain.concert.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.dto.ConcertTodayResponse;
import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertDetailRepository concertDetailRepository;
    private final ConcertRepository concertRepository;

    public ConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "존재하지 않는 공연입니다. id=" + concertId));

        List<ConcertDetail> details = concertDetailRepository.findAllByConcertOrderByStartAtAsc(concert);

        return ConcertResponse.of(concert, details);
    }

    public ConcertDetailResponse getConcertDetail(Long detailId) {
        ConcertDetail concertDetail = concertDetailRepository.findByIdWithConcert(detailId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "존재하지 않는 공연 회차입니다. id=" + detailId));

        return ConcertDetailResponse.from(concertDetail);
    }

    public List<ConcertResponse> getRecentConcerts() {
        return concertRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(ConcertResponse::from)
                .toList();
    }

    public List<ConcertTodayResponse> getTodayConcerts() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return concertDetailRepository.findByStartAtBetween(startOfDay, endOfDay)
                .stream()
                .map(ConcertTodayResponse::from)
                .toList();
    }

    public Page<ConcertResponse> searchConcerts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return concertRepository.findByNameContainingIgnoreCase(query, pageable)
                .map(ConcertResponse::from);
    }

}