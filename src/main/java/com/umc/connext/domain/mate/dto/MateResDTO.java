package com.umc.connext.domain.mate.dto;

import com.umc.connext.domain.member.entity.Member;
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
            Long memberId,
            String nickname,
            String profileImage
    ){
        public static MateListResDTO from(Member member) {
            return new MateListResDTO(
                    member.getId(),
                    member.getNickname(),
                    member.getProfileImage()
            );
        }
    }

}
