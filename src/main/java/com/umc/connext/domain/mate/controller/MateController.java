package com.umc.connext.domain.mate.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mate.dto.MateReqDTO;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.domain.mate.service.MateService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mate", description = "친구 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mates")
public class MateController implements MateControllerDocs {

    private final MateService mateService;

    // 친구 요청
    @PostMapping("/request")
    @Override
    public ResponseEntity<Response<MateResDTO.MateRequestResDTO>> requestMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MateReqDTO.MateRequestDTO dto
    ) {
        Long requesterId = userDetails.getMemberId();
        Long addresseeId = dto.addresseeId();

        MateResDTO.MateRequestResDTO result = mateService.sendMateRequest(requesterId, addresseeId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.CREATED, result, "친구 요청 성공"));
    }

    // 친구 요청 수락
    @PostMapping("/{mateId}/accept")
    public ResponseEntity<Response<Void>> acceptMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.acceptMateRequest(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.UPDATE_SUCCESS, "친구 요청 수락 성공"));
    }

    // 친구 요청 거절
    @PostMapping("/{mateId}/reject")
    public ResponseEntity<Response<Void>> rejectMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    ) {
        Long memberId = userDetails.getMemberId();
        mateService.rejectMateRequest(memberId, mateId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.UPDATE_SUCCESS, "친구 요청 거절 성공"));
    }

    // 친구 목록 조회
    @GetMapping("")
    @Override
    public ResponseEntity<Response<List<MateResDTO.MateListResDTO>>> getMyMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<MateResDTO.MateListResDTO> result = mateService.getMyMates(memberId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, result, "친구 목록 조회 성공"));
    }
}
