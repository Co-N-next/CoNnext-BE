package com.umc.connext.domain.concert.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Concert", description = "공연 관련 API")
@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @Operation(summary = "공연 기본 정보 조회", description = "공연의 포스터, 제목, 설명 등 기본 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공연을 찾을 수 없음")
    })
    @GetMapping("/{concertId}")
    public ResponseEntity<Response<ConcertResponse>> getConcert(
            @Parameter(description = "공연 ID", example = "1")
            @PathVariable Long concertId
    ) {
        ConcertResponse result = concertService.getConcert(concertId);

        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "공연 정보 조회 성공"));
    }

    @Operation(summary = "공연 상세 회차 조회", description = "특정 공연 회차(Detail ID)의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 회차를 찾을 수 없음")
    })
    @GetMapping("/details/{detailId}")
    public ResponseEntity<Response<ConcertDetailResponse>> getConcertDetail(
            @Parameter(description = "공연 상세(회차) ID", example = "1")
            @PathVariable Long detailId
    ) {
        // Service에 detailId 전달
        ConcertDetailResponse result = concertService.getConcertDetail(detailId);

        Response<ConcertDetailResponse> response = Response.success(
                SuccessCode.GET_SUCCESS,
                result,
                "공연 상세 회차 조회 성공"
        );
        return ResponseEntity.ok().body(response);
    }
}