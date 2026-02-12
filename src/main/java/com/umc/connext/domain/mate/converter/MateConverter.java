package com.umc.connext.domain.mate.converter;

import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.entity.Mate;
import com.umc.connext.domain.member.entity.Member;

public class MateConverter {

    // DTO -> Entity
    public static Mate toMate(
            Member requester,
            Member addressee
    ){
        return Mate.request(requester, addressee);
    }

    // Entity -> DTO
    public static MateResDTO.MateRequestResDTO toMateRequestResDTO(
            Mate mate
    ){
        return MateResDTO.MateRequestResDTO.builder()
                .mateId(mate.getId())
                .requesterId(mate.getRequester().getId())
                .addresseeId(mate.getAddressee().getId())
                .status(mate.getStatus().name())
                .build();
    }

}
