package com.umc.connext.domain.mate.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.concert.repository.ConcertDetailRepository;
import com.umc.connext.domain.mate.converter.MateConverter;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.dto.TodayMateResDTO;
import com.umc.connext.domain.mate.entity.FavoriteMate;
import com.umc.connext.domain.mate.entity.Mate;
import com.umc.connext.domain.mate.enums.MateStatus;
import com.umc.connext.domain.mate.repository.FavoriteMateRepository;
import com.umc.connext.domain.mate.repository.MateRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.reservation.dto.SeatInfoDTO;
import com.umc.connext.domain.reservation.entity.Reservation;
import com.umc.connext.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final MemberRepository memberRepository;
    private final FavoriteMateRepository favoriteMateRepository;
    private final ConcertDetailRepository concertDetailRepository;
    private final ReservationRepository reservationRepository;

    // 메이트 요청
    @Transactional
    public MateResDTO.MateRequestResDTO sendMateRequest(Long requesterId, Long addresseeId) {

        // 자기 자신에게 메이트 요청 불가
        if (requesterId.equals(addresseeId)) {
            throw new GeneralException(ErrorCode.BAD_REQUEST, "자기 자신에게 메이트 요청을 보낼 수 없습니다.");
        }

        // Member 조회
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> GeneralException.notFound("요청자 멤버를 찾을 수 없습니다."));
        Member addressee = memberRepository.findById(addresseeId)
                .orElseThrow(() -> GeneralException.notFound("수신자 멤버를 찾을 수 없습니다."));

        // 기존 메이트 요청 확인
        mateRepository.findBetween(requesterId, addresseeId)
                .ifPresent(mate -> {
                    switch (mate.getStatus()) {
                        case PENDING -> throw new GeneralException(ErrorCode.CONFLICT, "이미 대기 중인 메이트 요청이 있습니다.");
                        case ACCEPTED -> throw new GeneralException(ErrorCode.CONFLICT, "이미 메이트인 사용자입니다.");
                        case REJECTED -> throw new GeneralException(ErrorCode.CONFLICT, "이미 거절된 메이트 요청이 있습니다.");
                        case BLOCKED -> throw new GeneralException(ErrorCode.FORBIDDEN, "차단된 사용자입니다.");
                        default -> throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR, "처리할 수 없는 상태입니다.");
                    }
        });

        // 메이트 요청 생성 및 저장
        Mate mate = Mate.request(requester, addressee);
        mateRepository.save(mate);

        return MateConverter.toMateRequestResDTO(mate);
    }

    // 메이트 요청 수락
    @Transactional
    public void acceptMateRequest(Long memberId, Long mateId) {
        // 메이트 요청 조회
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("메이트 요청을 찾을 수 없습니다."));

        // 수신자 확인
        if (!mate.getAddressee().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "메이트 요청을 수락할 권한이 없습니다.");
        }

        // 상태 확인
        if (mate.getStatus() != MateStatus.PENDING) {
            throw new GeneralException(ErrorCode.CONFLICT, "대기 중인 메이트 요청만 수락할 수 있습니다.");
        }

        // 메이트 요청 수락
        mate.accept();
    }

    @Transactional
    public void rejectMateRequest(Long mateId, Long memberId) {
        // 메이트 요청 조회
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("메이트 요청을 찾을 수 없습니다."));

        // 수신자 확인
        if (!mate.getAddressee().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "메이트 요청을 거절할 권한이 없습니다.");
        }

        // 상태 확인
        if (mate.getStatus() != MateStatus.PENDING) {
            throw new GeneralException(ErrorCode.CONFLICT, "대기 중인 메이트 요청만 거절할 수 있습니다.");
        }

        mate.reject();
    }

    // 메이트 목록 조회
    @Transactional(readOnly = true)
    public List<MateResDTO.MateListResDTO> getMyMates(Long memberId) {
        List<Mate> mates = mateRepository.findAllAcceptedMatesByMemberId(memberId);
        List<FavoriteMate> favoriteMates = favoriteMateRepository.findAllByMemberId(memberId);
        Set<Long> favoriteMateIds = favoriteMates.stream()
                .map(fav -> fav.getMate().getId())
                .collect(Collectors.toSet());

        return mates.stream()
                .map(mate -> {
                    Member friend =
                            mate.getRequester().getId().equals(memberId) ?
                            mate.getAddressee() :
                            mate.getRequester();
                    return MateResDTO.MateListResDTO.from(mate, friend, favoriteMateIds.contains(mate.getId()));
                })
                .toList();
    }

    // 메이트 삭제
    @Transactional
    public void deleteMate(Long memberId, Long mateId) {
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("메이트 관계를 찾을 수 없습니다."));

        // 권한 확인
        if (!isParticipant(mate, memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "메이트 삭제 권한이 없습니다.");
        }

        // 메이트 상태일 때에만 삭제 가능
        if (mate.getStatus() != MateStatus.ACCEPTED) {
            throw new GeneralException(ErrorCode.CONFLICT, "메이트 관계가 아닌 사용자입니다.");
        }

        // 즐겨찾기 해제
        favoriteMateRepository.deleteAllByMateId(mateId);

        // 메이트 삭제
        mateRepository.delete(mate);
    }

    // 메이트 검색
    @Transactional(readOnly = true)
    public Page<MateResDTO.MateSearchResDTO> searchMates(Long me, String keyword, Integer page) {

        PageRequest pageRequest = PageRequest.of(page, 10);

        Page<Member> membersPage =
                memberRepository.findByNicknameContainingIgnoreCaseAndIdNotOrderByNicknameAsc(
                        keyword.trim(), me, pageRequest
                );

        List<Long> ids = membersPage.getContent().stream().map(Member::getId).toList();

        Map<Long, Mate> relationMap = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Mate mate : mateRepository.findRelationsWith(me, ids)) {
                Long otherId = mate.getRequester().getId().equals(me)
                        ? mate.getAddressee().getId()
                        : mate.getRequester().getId();
                relationMap.put(otherId, mate);
            }
        }

        return membersPage.map(member -> {
            Mate mate = relationMap.get(member.getId());

            if (mate == null) {
                return new MateResDTO.MateSearchResDTO(
                        member.getId(),
                        member.getNickname(),
                        member.getProfileImage(),
                        MateResDTO.MateSearchResDTO.RelationStatus.NONE,
                        null
                );
            }

            MateResDTO.MateSearchResDTO.RelationStatus status = switch (mate.getStatus()) {
                case ACCEPTED -> MateResDTO.MateSearchResDTO.RelationStatus.ACCEPTED;
                case BLOCKED -> MateResDTO.MateSearchResDTO.RelationStatus.BLOCKED;
                case PENDING -> mate.getRequester().getId().equals(me)
                        ? MateResDTO.MateSearchResDTO.RelationStatus.PENDING_SENT
                        : MateResDTO.MateSearchResDTO.RelationStatus.PENDING_RECEIVED;
                case REJECTED -> MateResDTO.MateSearchResDTO.RelationStatus.NONE;
            };

            return new MateResDTO.MateSearchResDTO(
                    member.getId(),
                    member.getNickname(),
                    member.getProfileImage(),
                    status,
                    mate.getId()
            );
        });
    }

    // ================= 자주 찾는 메이트 =================//
    private boolean isParticipant(Mate mate, Long memberId) {
        return mate.getRequester().getId().equals(memberId)
                || mate.getAddressee().getId().equals(memberId);
    }

    // 자주 찾는 메이트 추가
    @Transactional
    public void addFavoriteMate(Long memberId, Long mateId) {
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("메이트 관계를 찾을 수 없습니다."));

        // 권한 확인
        if (!isParticipant(mate, memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "즐겨찾기 추가 권한이 없습니다.");
        }

        // 상태 확인
        if (mate.getStatus() != MateStatus.ACCEPTED) {
            throw new GeneralException(ErrorCode.CONFLICT, "메이트 관계가 아닌 사용자입니다.");
        }

        // 중복 체크
        if (favoriteMateRepository.existsByMemberIdAndMateId(memberId, mateId)) {
            throw new GeneralException(ErrorCode.CONFLICT, "이미 즐겨찾기에 추가된 메이트입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.notFound("회원을 찾을 수 없습니다."));

        favoriteMateRepository.save(new FavoriteMate(null, member, mate));
    }

    // 자주 찾는 메이트 해제
    @Transactional
    public void removeFavoriteMate(Long memberId, Long mateId) {
        FavoriteMate favoriteMate = favoriteMateRepository.findByMemberIdAndMateId(memberId, mateId)
                .orElseThrow(() -> GeneralException.notFound("즐겨찾기된 메이트를 찾을 수 없습니다."));

        // 권한 확인
        Mate mate = favoriteMate.getMate();
        if (!isParticipant(mate, memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "즐겨찾기 해제 권한이 없습니다.");
        }

        favoriteMateRepository.delete(favoriteMate);
    }

    // 자주 찾는 메이트 목록 조회
    @Transactional(readOnly = true)
    public List<MateResDTO.FavoriteMateResDTO> getFavoriteMates(Long memberId) {
        List<Mate> mates = mateRepository.findAllAcceptedMatesByMemberId(memberId);

        Set<Long> favoriteMateIds = favoriteMateRepository.findAllByMemberId(memberId).stream()
                .map(fav -> fav.getMate().getId())
                .collect(Collectors.toSet());

        return mates.stream()
                .map(mate -> {
                    Member friend = mate.getRequester().getId().equals(memberId)
                            ? mate.getAddressee()
                            : mate.getRequester();
                    boolean isFavorite = favoriteMateIds.contains(mate.getId());
                    return new MateResDTO.FavoriteMateResDTO(
                            friend.getId(),
                            friend.getNickname(),
                            friend.getProfileImage(),
                            isFavorite
                    );
                }).toList();
    }

    // ==========================================
    // 오늘의 공연 메이트
    public List<TodayMateResDTO> getTodayMates(Long memberId) {
        // 오늘의 공연 조회
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Reservation todayConcert = reservationRepository
                .findFirstByMemberIdAndConcertDetail_StartAtBetweenOrderByConcertDetail_StartAtAsc(
                        memberId, startOfDay, endOfDay
                ).orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND, "오늘 예정된 공연이 없습니다."));

        Long todayConcertDetailId = todayConcert.getConcertDetail().getId();

        // 오늘 공연을 함께 보는 메이트 조회
        return mateRepository.findTodayMatesByMemberId(memberId, todayConcertDetailId);
    }

    // 메이트 프로필 조회
    public MateResDTO.MateProfileResDTO getMateProfile(Long memberId, Long mateId) {
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("메이트 관계를 찾을 수 없습니다."));

        // 권한 확인
        if (!isParticipant(mate, memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "메이트 프로필 조회 권한이 없습니다.");
        }

        // 상대 유저 찾기
        var partner = mate.getRequester().getId().equals(memberId) ? mate.getAddressee() : mate.getRequester();

        // 자주 찾는 메이트 설정 여부
        boolean isFavorite = favoriteMateRepository.existsByMemberIdAndMateId(memberId, mateId);

        // 상대 예약 목록 조회
        List<MateResDTO.MateReservationSummaryDTO> reservations =
                reservationRepository.findReservationSummariesByMemberId(partner.getId())
                        .stream()
                        .map(p -> MateResDTO.MateReservationSummaryDTO.builder()
                                .concertName(p.getConcertName())
                                .concertPosterImage(p.getConcertPosterImage())
                                .concertArtist(p.getConcertArtist())
                                .startAt(p.getStartAt())
                                .concertVenue(p.getConcertVenue())
                                .seatInfo(
                                        SeatInfoDTO.builder()
                                                .floor(p.getFloor())
                                                .section(p.getSection())
                                                .row(p.getRow())
                                                .seat(p.getSeat())
                                                .build()
                                )
                                .build()
                        )
                        .toList();

        return MateResDTO.MateProfileResDTO.builder()
                .mateId(mate.getId())
                .memberId(partner.getId())
                .nickname(partner.getNickname())
                .profileImage(partner.getProfileImage())
                .isFavorite(isFavorite)
                .reservations(reservations)
                .build();

    }
}
