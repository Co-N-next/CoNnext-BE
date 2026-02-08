package com.umc.connext.domain.mate.service;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.domain.mate.converter.MateConverter;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.entity.Mate;
import com.umc.connext.domain.mate.enums.MateStatus;
import com.umc.connext.domain.mate.repository.MateRepository;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final MemberRepository memberRepository;

    // 친구 요청
    @Transactional
    public MateResDTO.MateRequestResDTO sendMateRequest(Long requesterId, Long addresseeId) {

        // 자기 자신에게 친구 요청 불가
        if (requesterId.equals(addresseeId)) {
            throw new GeneralException(ErrorCode.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // Member 조회
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> GeneralException.notFound("요청자 멤버를 찾을 수 없습니다."));
        Member addressee = memberRepository.findById(addresseeId)
                .orElseThrow(() -> GeneralException.notFound("수신자 멤버를 찾을 수 없습니다."));

        // 기존 친구 요청 확인
        mateRepository.findBetween(requesterId, addresseeId)
                .ifPresent(mate -> {
                    switch (mate.getStatus()) {
                        case PENDING -> throw new GeneralException(ErrorCode.CONFLICT, "이미 대기 중인 친구 요청이 있습니다.");
                        case ACCEPTED -> throw new GeneralException(ErrorCode.CONFLICT, "이미 친구인 사용자입니다.");
                        case REJECTED -> throw new GeneralException(ErrorCode.CONFLICT, "이미 거절된 친구 요청이 있습니다.");
                        case BLOCKED -> throw new GeneralException(ErrorCode.FORBIDDEN, "차단된 사용자입니다.");
                        default -> throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR, "처리할 수 없는 상태입니다.");
                    }
        });

        // 친구 요청 생성 및 저장
        Mate mate = Mate.request(requester, addressee);
        mateRepository.save(mate);

        return MateConverter.toMateRequestResDTO(mate);
    }

    // 친구 요청 수락
    @Transactional
    public void acceptMateRequest(Long memberId, Long mateId) {
        // 친구 요청 조회
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("친구 요청을 찾을 수 없습니다."));

        // 수신자 확인
        if (!mate.getAddressee().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "친구 요청을 수락할 권한이 없습니다.");
        }

        // 상태 확인
        if (mate.getStatus() != MateStatus.PENDING) {
            throw new GeneralException(ErrorCode.CONFLICT, "대기 중인 친구 요청만 수락할 수 있습니다.");
        }

        // 친구 요청 수락
        mate.accept();
    }

    @Transactional
    public void rejectMateRequest(Long mateId, Long memberId) {
        // 친구 요청 조회
        Mate mate = mateRepository.findById(mateId)
                .orElseThrow(() -> GeneralException.notFound("친구 요청을 찾을 수 없습니다."));

        // 수신자 확인
        if (!mate.getAddressee().getId().equals(memberId)) {
            throw new GeneralException(ErrorCode.FORBIDDEN, "친구 요청을 거절할 권한이 없습니다.");
        }

        // 상태 확인
        if (mate.getStatus() != MateStatus.PENDING) {
            throw new GeneralException(ErrorCode.CONFLICT, "대기 중인 친구 요청만 거절할 수 있습니다.");
        }

        mate.reject();
    }

    // 친구 목록 조회
    @Transactional(readOnly = true)
    public List<MateResDTO.MateListResDTO> getMyMates(Long memberId) {
        List<Mate> mates = mateRepository.findAllAcceptedMatesByMemberId(memberId);

        return mates.stream()
                .map(mate -> {
                    Member friend =
                            mate.getRequester().getId().equals(memberId) ?
                            mate.getAddressee() :
                            mate.getRequester();
                    return MateResDTO.MateListResDTO.from(friend);
                })
                .toList();
    }
}
