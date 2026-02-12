package com.umc.connext.domain.concert.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.dto.*;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertDetailRepository concertDetailRepository;
    private final ConcertRepository concertRepository;

    @Transactional
    public ConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "존재하지 않는 공연입니다. id=" + concertId));

        incrementViewCount(concertId);
        List<ConcertDetail> details = concertDetailRepository.findAllByConcertOrderByStartAtAsc(concert);

        return ConcertResponse.of(concert, details);
    }

    @Transactional
    public ConcertDetailResponse getConcertDetail(Long detailId) {
        ConcertDetail concertDetail = concertDetailRepository.findByIdWithConcert(detailId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "존재하지 않는 공연 회차입니다. id=" + detailId));

        Concert concert = concertDetail.getConcert();
        if (concert != null && concert.getId() != null) {
            concertRepository.incrementViewCount(concert.getId());
            concert.increaseViewCount();
        }

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

    public Page<UpcomingConcertResponse> getUpcomingConcerts(int page, int size, String sortBy) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable;

        if ("popular".equalsIgnoreCase(sortBy)) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Concert> concerts = concertRepository.findUpcomingConcerts(now, pageable);

        if (concerts.isEmpty()) {
            return concerts.map(concert -> UpcomingConcertResponse.of(concert, null, concert.getViewCount()));
        }

        Map<Long, LocalDateTime> nextShowTimeMap = concertDetailRepository
                .findNextShowTimes(concerts.getContent(), now)
                .stream()
                .collect(Collectors.toMap(
                        ConcertStartResponse::concertId, // dto.concertId() (record인 경우)
                        ConcertStartResponse::startAt    // dto.startAt()
                ));

        return concerts.map(concert -> UpcomingConcertResponse.of(
                concert,
                nextShowTimeMap.get(concert.getId()),
                concert.getViewCount()
        ));
    }

    @Transactional
    public void incrementViewCount(Long concertId) {
        concertRepository.incrementViewCount(concertId);
    }

}

