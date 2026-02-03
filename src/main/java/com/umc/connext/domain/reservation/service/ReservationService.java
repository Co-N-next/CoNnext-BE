package com.umc.connext.domain.reservation.service;

import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.entity.ConcertDetail;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.converter.ReservationConverter;
import com.umc.connext.domain.reservation.dto.ReservationReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.entity.Reservation;
import com.umc.connext.domain.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertDetailRepository concertDetailRepository;

    @PersistenceContext
    private EntityManager em;

    // 예매내역 생성
    @Transactional
    public ReservationResDTO.ReservationAddResDTO addReservation(
            Long memberId,
            ReservationReqDTO.ReservationAddReqDTO reqDTO
    ) {
        // 회원 존재 확인
        Member member = em.find(Member.class, memberId);
        if (member == null) throw GeneralException.notFound("존재하지 않는 회원입니다.");

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
        Member member = em.find(Member.class, memberId);
        if (member == null) throw GeneralException.notFound("존재하지 않는 회원입니다.");

        reservationRepository.deleteByIdAndMemberId(reservationId, memberId);
    }

}
