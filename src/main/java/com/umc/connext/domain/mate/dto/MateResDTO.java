package com.umc.connext.domain.mate.dto;

import com.umc.connext.domain.mate.entity.Mate;
import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.reservation.dto.SeatInfoDTO;
import lombok.Builder;

public class MateResDTO {

    @Builder
    public record MateRequestResDTO(
            Long mateId,
            Long requesterId,
            Long addresseeId,
            String status
    ){}

    @Builder
    public record MateListResDTO(
            Long mateId,
            Long memberId,
            String nickname,
            String profileImage,
            boolean isFavorite
    ){
        public static MateListResDTO from(
                Mate mate,
                Member m,
                boolean isFavorite
        ) {
            Member friend = mate.getRequester().equals(m) ? mate.getAddressee() : mate.getRequester();

            return MateListResDTO.builder()
                    .mateId(mate.getId())
                    .memberId(friend.getId())
                    .nickname(friend.getNickname())
                    .profileImage(friend.getProfileImage())
                    .isFavorite(isFavorite)
                    .build();
        }
    }

    @Builder
    public record FavoriteMateResDTO(
            Long memberId,
            String nickname,
            String profileImage,
            boolean isFavorite
    ){}

    @Builder
    public record MateSearchResDTO(
            Long memberId,
            String nickname,
            String profileImage,
            RelationStatus relationStatus,
            Long mateId // null일 수 있음
    ) {
        public enum RelationStatus {
            NONE,
            PENDING_SENT,
            PENDING_RECEIVED,
            ACCEPTED,
            BLOCKED
        }
    }
}
