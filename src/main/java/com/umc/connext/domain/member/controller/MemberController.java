package com.umc.connext.domain.member.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.dto.UsernameValidationCheckDto;
import com.umc.connext.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/check-username")
    public ResponseEntity<Response<Void>> checkUsername(@RequestParam @Valid UsernameValidationCheckDto usernameValidationCheckDto) {
        memberService.checkUsernameDuplicate(usernameValidationCheckDto.getUsername());
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_USERNAME.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_USERNAME));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Response<Void>> checkNickname(
            @RequestParam String nickname
    ) {
        memberService.checkNicknameDuplicate(nickname);
        return ResponseEntity
                .status(SuccessCode.AVAILABLE_NICKNAME.getStatusCode())
                .body(Response.success(SuccessCode.AVAILABLE_NICKNAME));
    }
}
