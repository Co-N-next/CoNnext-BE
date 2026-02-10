package com.umc.connext.domain.reservation.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.reservation.converter.ReservationConverter;
import com.umc.connext.domain.reservation.dto.*;
import com.umc.connext.domain.reservation.entity.Reservation;
import com.umc.connext.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertDetailRepository concertDetailRepository;
    private final MemberRepository memberRepository;

    // 예매내역 생성
    @Transactional
    public ReservationResDTO.ReservationAddResDTO addReservation(
            Long memberId,
            ReservationAddReqDTO reqDTO
    ) {
        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        // 공연 상세 정보 확인
        ConcertDetail concertDetail = concertDetailRepository.findById(reqDTO.concertDetailId())
                .orElseThrow(() -> GeneralException.notFound("공연 상세 정보를 찾을 수 없습니다."));

        // 같은 정보를 저장할 경우
        Optional<Reservation> existing = reservationRepository.findByMemberIdAndConcertDetailIdAndFloorAndSectionAndRowAndSeat(memberId, reqDTO.concertDetailId(), reqDTO.seatInfo().floor(), reqDTO.seatInfo().section(), reqDTO.seatInfo().row(), reqDTO.seatInfo().seat());
        if (existing.isPresent()) {
            return ReservationConverter.toReservationAddResDTO(existing.get()); // 멱등 처리
        }

        // 예매내역 생성
        Reservation reservation = ReservationConverter.toReservation(reqDTO, member, concertDetail);
        Reservation saved = reservationRepository.save(reservation);

        // DTO 반환
        return ReservationConverter.toReservationAddResDTO(saved);

    }

    // 예매내역 삭제
    @Transactional
    public void deleteReservation(
            Long memberId,
            Long reservationId
    ){
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        // 예매내역 존재 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 예매내역입니다."));

        // 예매내역 소유자 확인
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "예매내역 삭제 권한이 없습니다.");
        }

        reservationRepository.delete(reservation);
    }

    // 예매내역 조회
    @Transactional(readOnly = true)
    public List<ReservationGetResDTO> getMyReservations(
            Long memberId
    ){
        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        List<ReservationGetResDTO> reservations = reservationRepository.findAllByMember(member);

        return reservations;
    }

    // 예매내역 수정
    @Transactional
    public ReservationResDTO.ReservationUpdateResDTO updateReservation(
            Long memberId,
            Long reservationId,
            ReservationUpdateReqDTO reqDTO
    ){
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 회원입니다."));

        // 예매내역 존재 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> GeneralException.notFound("존재하지 않는 예매내역입니다."));

        // 예매내역 소유자 확인
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "예매내역 수정 권한이 없습니다.");
        }

        // concertDetail 변경
        if (reqDTO.concertDetailId() != null && !reqDTO.concertDetailId().equals(reservation.getConcertDetail().getId())) {
            ConcertDetail newDetail = concertDetailRepository.findById(reqDTO.concertDetailId())
                    .orElseThrow(() -> GeneralException.notFound("공연 상세 정보를 찾을 수 없습니다."));
            reservation.changeConcertDetail(newDetail);
        }

        // 좌석 정보 변경
        if (reqDTO.seatInfo() != null) {
            Integer floor = reqDTO.seatInfo().floor() != null ? reqDTO.seatInfo().floor() : reservation.getFloor();
            String section = reqDTO.seatInfo().section() != null ? reqDTO.seatInfo().section() : reservation.getSection();
            String row = reqDTO.seatInfo().row() != null ? reqDTO.seatInfo().row() : reservation.getRow();
            Integer seat = reqDTO.seatInfo().seat() != null ? reqDTO.seatInfo().seat() : reservation.getSeat();

            reservation.changeSeatInfo(floor, section, row, seat);
        }

        return ReservationResDTO.ReservationUpdateResDTO.builder()
                .reservationId(reservation.getId())
                .concertDetailId(reservation.getConcertDetail().getId())
                .seat(new SeatInfoDTO(
                        reservation.getFloor(),
                        reservation.getSection(),
                        reservation.getRow(),
                        reservation.getSeat()
                ))
                .build();
    }

}
