package com.umc.connext.domain.member.controller;

import com.umc.connext.common.code.ErrorCode;
import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.exception.GeneralException;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.member.dto.NotificationSettingRequestDTO;
import com.umc.connext.domain.member.dto.NotificationSettingResponseDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingRequestDTO;
import com.umc.connext.domain.member.dto.VisibilitySettingResponseDTO;
import com.umc.connext.domain.member.service.MemberSettingService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members/me/settings")
@RequiredArgsConstructor
@Tag(name = "Member Settings", description = "회원 공개 범위 및 알림 설정 API")
public class MemberSettingController {

    private final MemberSettingService memberSettingService;

    @Operation(
            summary = "공개 범위 설정 조회",
            description = "로그인한 회원의 공연 및 좌석 공개 범위 설정을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 범위 설정 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/visibility")
    public ResponseEntity<Response<VisibilitySettingResponseDTO>> getVisibility(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        VisibilitySettingResponseDTO result = memberSettingService.getVisibility(userDetails.getMemberId());
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result,"공개 범위 설정 조회 성공"));
    }

    @Operation(
            summary = "공개 범위 설정 수정",
            description = "로그인한 회원의 공연 공개 범위 및 좌석 공개 단계를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 범위 설정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PatchMapping("/visibility")
    public ResponseEntity<Response<Void>> updateVisibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid VisibilitySettingRequestDTO req
    ) {

        if (userDetails == null) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        memberSettingService.updateVisibility(userDetails.getMemberId(), req);

        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.UPDATE_SUCCESS,"공개 범위 설정 수정 성공"));
    }

    @Operation(
            summary = "알림 설정 조회",
            description = "로그인한 회원의 서비스/푸시/SMS 알림 설정을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 설정 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/notifications")
    public ResponseEntity<Response<NotificationSettingResponseDTO>> getNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        NotificationSettingResponseDTO result = memberSettingService.getNotification(userDetails.getMemberId());

        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "알림 설정 조회 성공"));
    }

    @Operation(
            summary = "알림 설정 수정",
            description = "로그인한 회원의 서비스/푸시/SMS 알림 설정을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 설정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PatchMapping("/notifications")
    public ResponseEntity<Response<Void>> updateNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid NotificationSettingRequestDTO req
    ) {

        if (userDetails == null) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        memberSettingService.updateNotification(userDetails.getMemberId(), req);

        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.UPDATE_SUCCESS,"알림 설정 수정 성공"));
    }
}
