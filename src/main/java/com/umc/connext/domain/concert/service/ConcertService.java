package com.umc.connext.domain.concert.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.dto.ConcertTodayResponse;
import com.umc.connext.domain.concert.dto.UpcomingConcertResponse;
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

    /**
     * 다가오는 공연 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 ("latest": 최신순, "popular": 조회수순)
     * @return 다가오는 공연 목록
     */
    public Page<UpcomingConcertResponse> getUpcomingConcerts(int page, int size, String sortBy) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable;
        Page<Concert> concerts;

        if ("popular".equalsIgnoreCase(sortBy)) {
            // 조회수 순
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
            concerts = concertRepository.findUpcomingConcertsOrderByViewCount(now, pageable);
        } else {
            // 최신순 (기본)
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            concerts = concertRepository.findUpcomingConcertsOrderByCreated(now, pageable);
        }

        return concerts.map(concert -> {
            LocalDateTime nextShowTime = concertDetailRepository.findNextShowTime(concert, now);
            return UpcomingConcertResponse.of(concert, nextShowTime, concert.getViewCount());
        });
    }

}

