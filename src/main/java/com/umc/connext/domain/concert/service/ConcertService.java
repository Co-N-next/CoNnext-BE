package com.umc.connext.domain.concert.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertMyTodayResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.dto.ConcertTodayResponse;
import com.umc.connext.domain.concert.dto.ConcertUpcomingResponse;
import com.umc.connext.domain.concert.entity.Concert;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.concert.repository.ConcertRepository;
import com.umc.connext.domain.reservation.entity.Reservation;
import com.umc.connext.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertDetailRepository concertDetailRepository;
    private final ConcertRepository concertRepository;
    private final ReservationRepository reservationRepository;

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

    public List<ConcertUpcomingResponse> getUpcomingConcerts() {
        return concertDetailRepository
                .findTop20ByStartAtAfterOrderByStartAtAsc(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .stream()
                .map(ConcertUpcomingResponse::from)
                .toList();
    }

    public List<ConcertMyTodayResponse> getMyTodayConcerts(Long memberId) {
        // 사용자의 오늘 예매 정보 조회
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();

        log.info("=== getMyTodayConcerts Debug Start ===");
        log.info("memberId: {}, today: {}", memberId, today);

        List<Reservation> reservations = reservationRepository.findMyTodayReservations(memberId, startOfDay);
        log.info("Found {} reservations for today", reservations.size());

        List<ConcertMyTodayResponse> result = reservations.stream()
                .map(reservation -> {
                    ConcertDetail detail = reservation.getConcertDetail();
                    Concert concert = detail.getConcert();

                    log.debug("Processing reservation ID: {}, concert: {}, startAt: {}",
                        reservation.getId(), concert.getName(), detail.getStartAt());

                    // 첫 번째 아티스트 이름 추출 (null-safe)
                    String artist = concert.getConcertCasts() != null && !concert.getConcertCasts().isEmpty()
                            ? concert.getConcertCasts().stream()
                                .findFirst()
                                .map(cc -> cc.getCast().getName())
                                .orElse("미정")
                            : "미정";

                    log.debug("Artist: {}, ConcertCasts size: {}", artist,
                        concert.getConcertCasts() != null ? concert.getConcertCasts().size() : 0);

                    // 첫 번째 공연장 이름 추출 (null-safe)
                    String venue = concert.getConcertVenues() != null && !concert.getConcertVenues().isEmpty()
                            ? concert.getConcertVenues().stream()
                                .findFirst()
                                .map(cv -> cv.getVenue().getName())
                                .orElse("미정")
                            : "미정";

                    log.debug("Venue: {}, ConcertVenues size: {}", venue,
                        concert.getConcertVenues() != null ? concert.getConcertVenues().size() : 0);

                    return ConcertMyTodayResponse.from(
                            detail,
                            artist,
                            venue,
                            reservation.getFloor(),
                            reservation.getSection(),
                            reservation.getRow(),
                            reservation.getSeat(),
                            reservation.getId()
                    );
                })
                .toList();

        log.info("=== getMyTodayConcerts Debug End: {} responses ===", result.size());
        return result;
    }

    // 진단용 메서드 - 데이터가 없을 때 이것을 사용해 보세요
    public List<ConcertMyTodayResponse> getMyTodayConcertsDebug(Long memberId) {
        List<Reservation> reservations = reservationRepository.findMyTodayReservationsSimple(memberId);

        return reservations.stream()
                .map(reservation -> {
                    ConcertDetail detail = reservation.getConcertDetail();
                    Concert concert = detail.getConcert();

                    // 첫 번째 아티스트 이름 추출 (null-safe)
                    String artist = concert.getConcertCasts() != null && !concert.getConcertCasts().isEmpty()
                            ? concert.getConcertCasts().stream()
                                .findFirst()
                                .map(cc -> cc.getCast().getName())
                                .orElse("미정")
                            : "미정";

                    // 첫 번째 공연장 이름 추출 (null-safe)
                    String venue = concert.getConcertVenues() != null && !concert.getConcertVenues().isEmpty()
                            ? concert.getConcertVenues().stream()
                                .findFirst()
                                .map(cv -> cv.getVenue().getName())
                                .orElse("미정")
                            : "미정";

                    return ConcertMyTodayResponse.from(
                            detail,
                            artist,
                            venue,
                            reservation.getFloor(),
                            reservation.getSection(),
                            reservation.getRow(),
                            reservation.getSeat(),
                            reservation.getId()
                    );
                })
                .toList();
    }

    // 진단용 메서드 - DB에 데이터가 있는지 확인
    public int countTodayReservations(Long memberId) {
        int count = reservationRepository.countTodayReservations(memberId);
        log.info("=== Diagnostic: countTodayReservations ===");
        log.info("memberId: {}, count: {}", memberId, count);
        return count;
    }
}