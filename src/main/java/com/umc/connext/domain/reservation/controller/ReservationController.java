package com.umc.connext.domain.reservation.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.reservation.dto.ReservationAddReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationGetResDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.dto.ReservationUpdateReqDTO;
import com.umc.connext.domain.reservation.service.ReservationService;
import com.umc.connext.global.jwt.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservation", description = "예매내역 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController implements ReservationControllerDocs {

    private final ReservationService reservationService;

    // 예매내역 추가
    @PostMapping("")
    @Override
    public ResponseEntity<Response<ReservationResDTO.ReservationAddResDTO>> addReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid @NotBlank ReservationAddReqDTO dto
    ){
        Long memberId = userDetails.getMemberId();

        return ResponseEntity.ok().body(Response.success(SuccessCode.INSERT_SUCCESS, reservationService.addReservation(memberId, dto), "예매내역 추가 성공"));
    }

    // 예매내역 삭제
    @DeleteMapping("/{reservationId}")
    @Override
    public ResponseEntity<Response<Void>> deleteReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ){

        Long memberId = userDetails.getMemberId();

        reservationService.deleteReservation(memberId, reservationId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "예매내역 삭제 성공"));
    }

    // 예매내역 조회
    @GetMapping("")
    @Override
    public ResponseEntity<Response<List<ReservationGetResDTO>>> myReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long memberId = userDetails.getMemberId();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, reservationService.getMyReservations(memberId), "예매내역 조회 성공"));
    }

    // 예매내역 수정
    @PatchMapping("/{reservationId}")
    @Override
    public ResponseEntity<Response<ReservationResDTO.ReservationUpdateResDTO>> updateReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long reservationId,
        @RequestBody ReservationUpdateReqDTO dto
    ){
        Long memberId = userDetails.getMemberId();

        return ResponseEntity.ok().body(Response.success(SuccessCode.GET_SUCCESS, reservationService.updateReservation(memberId, reservationId, dto), "예매내역 수정 성공"));
    }

}
