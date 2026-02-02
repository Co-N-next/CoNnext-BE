package com.umc.connext.domain.reservation.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.reservation.dto.ReservationReqDTO;
import com.umc.connext.domain.reservation.dto.ReservationResDTO;
import com.umc.connext.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
