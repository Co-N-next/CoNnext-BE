package com.umc.connext.domain.reservation.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.reservation.dto.ReservationReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservation", description = "예매내역 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController implements ReservationControllerDocs {

    private final ReservationService reservationService;

    // 예매내역 추가
    @PostMapping("")
    public ResponseEntity<Response<ReservationResDTO.ReservationAddResDTO>> addReservation(
            @RequestBody ReservationReqDTO.ReservationAddReqDTO dto
    ){
        Long memberId = 1L; // 임시 회원 (추후 삭제)

        return ResponseEntity.ok().body(Response.success(SuccessCode.INSERT_SUCCESS, reservationService.addReservation(memberId, dto), "예매내역 추가 성공"));
    }

    // 예매내역 삭제
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Response> deleteReservation(
            @PathVariable Long reservationId
    ){

        Long memberId = 1L; // 임시 회원 (추후 삭제)

        reservationService.deleteReservation(memberId, reservationId);

        return ResponseEntity.ok().body(Response.success(SuccessCode.DELETE_SUCCESS, "예매내역 삭제 성공"));
    }

}
