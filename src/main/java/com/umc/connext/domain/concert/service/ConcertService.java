package com.umc.connext.domain.concert.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new GeneralException(ErrorCode.BAD_REQUEST, "존재하지 않는 공연 회차입니다. id=" + detailId));

        return ConcertDetailResponse.from(concertDetail);
    }
}