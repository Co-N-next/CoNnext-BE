package com.umc.connext.domain.reservation.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.reservation.dto.ReservationGetResDTO;
import com.umc.connext.domain.reservation.dto.ReservationReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/reservations")
public interface ReservationControllerDocs {

    @Operation(
            summary = "예매내역 추가",
            description = "공연 상세 정보를 바탕으로 예매내역을 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "공연 정보를 찾을 수 없습니다.")
    })
    @PostMapping("")
    ResponseEntity<Response<ReservationResDTO.ReservationAddResDTO>> addReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReservationReqDTO.ReservationAddReqDTO dto
    );

    @Operation(
            summary = "예매내역 삭제",
            description = "예매내역을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "예매내역을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{reservationId}")
    ResponseEntity<Response> deleteReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    );

    // 예매내역 조회
    @Operation(
            summary = "예매내역 조회",
            description = "내 예매내역을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("")
    ResponseEntity<Response<List<ReservationGetResDTO>>> myReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

}
