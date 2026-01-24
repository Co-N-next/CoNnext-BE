package com.umc.connext.domain.member.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.dto.NicknameDTO;
import com.umc.connext.domain.member.service.NicknameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class NicknameController {

    private final NicknameService nicknameService;

    @GetMapping("/nickname/random")
    public ResponseEntity<Response<NicknameDTO>> generateRandomNickname() {
        String nickname = nicknameService.generateRandomNickname();
        return ResponseEntity
                .status(SuccessCode.NICKNAME_GENERATION_SUCCESS.getStatusCode())
                .body(Response.success(SuccessCode.NICKNAME_GENERATION_SUCCESS,
                        NicknameDTO.of(nickname)));
    }
}
