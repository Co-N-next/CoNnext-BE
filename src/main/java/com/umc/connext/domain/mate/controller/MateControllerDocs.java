package com.umc.connext.domain.mate.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.mate.dto.MateReqDTO;
import com.umc.connext.domain.mate.dto.MateResDTO;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/mates")
public interface MateControllerDocs {

    // 친구 요청
    @Operation(
            summary = "친구 요청",
            description = "다른 회원에게 친구 요청을 보냅니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "친구 요청 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음")
    })
    @PostMapping("/request")
    ResponseEntity<Response<MateResDTO.MateRequestResDTO>> requestMate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MateReqDTO.MateRequestDTO dto
    );

    // 친구 요청 수락
    @Operation(
            summary = "친구 요청 수락",
            description = "받은 친구 요청을 수락합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공"),
            @ApiResponse(responseCode = "404", description = "친구 요청이 존재하지 않습니다.")
    })
    @PostMapping("/{mateId}/accept")
    ResponseEntity<Response<Void>> acceptMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 친구 요청 거절
    @Operation(
            summary = "친구 요청 거절",
            description = "받은 친구 요청을 거절합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공"),
            @ApiResponse(responseCode = "404", description = "친구 요청이 존재하지 않습니다.")
    })
    @PostMapping("/{mateId}/reject")
    ResponseEntity<Response<Void>> rejectMateRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mateId
    );

    // 친구 목록 조회
    @Operation(
            summary = "친구 목록 조회",
            description = "내 친구 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구 목록 조회 성공")
    })
    @GetMapping("")
    ResponseEntity<Response<List<MateResDTO.MateListResDTO>>> getMyMates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

}
