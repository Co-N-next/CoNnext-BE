package com.umc.connext.domain.concert.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.concert.dto.ConcertDetailResponse;
import com.umc.connext.domain.concert.dto.ConcertResponse;
import com.umc.connext.domain.concert.dto.ConcertTodayResponse;
import com.umc.connext.domain.concert.dto.UpcomingConcertResponse;
import com.umc.connext.domain.concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Concert", description = "공연 관련 API")
@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @Operation(summary = "최신 공연 조회", description = "최근 등록된 공연 10개를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/recent")
    public ResponseEntity<Response<List<ConcertResponse>>> getRecentConcerts() {
        List<ConcertResponse> result = concertService.getRecentConcerts();
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "최신 공연 조회 성공"));
    }

    @Operation(summary = "오늘 공연 조회", description = "오늘 진행되는 공연 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/today")
    public ResponseEntity<Response<List<ConcertTodayResponse>>> getTodayConcerts() {
        List<ConcertTodayResponse> result = concertService.getTodayConcerts();
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "오늘 공연 조회 성공"));
    }

    @Operation(summary = "공연 검색", description = "공연명으로 공연을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<Response<List<ConcertResponse>>> searchConcerts(
            @Parameter(description = "검색어", example = "콘서트")
            @RequestParam(defaultValue = "") String query,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ConcertResponse> result = concertService.searchConcerts(query, page, size);
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "공연 검색 성공"));
    }

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
        ConcertDetailResponse result = concertService.getConcertDetail(detailId);
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "공연 상세 회차 조회 성공"));
    }

    @Operation(summary = "다가오는 공연 목록 조회", description = "현재 시간 이후의 공연들을 조회합니다. 정렬 기준: latest(최신순), popular(조회수순)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/upcoming")
    public ResponseEntity<Response<List<UpcomingConcertResponse>>> getUpcomingConcerts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (latest: 최신순, popular: 조회수순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        Page<UpcomingConcertResponse> result = concertService.getUpcomingConcerts(page, size, sortBy);
        return ResponseEntity.ok()
                .body(Response.success(SuccessCode.GET_SUCCESS, result, "다가오는 공연 목록 조회 성공"));
    }

    // TODO: 예매 기능 추가 후 구현 필요
    // @Operation(summary = "다가오는 내 공연 조회", description = "내가 예매한 다가오는 공연 3개를 조회합니다.")
    // @GetMapping("/my-upcoming")
    // public ResponseEntity<Response<List<UpcomingConcertResponse>>> getMyUpcomingConcerts(
    //         @AuthenticationPrincipal CustomUserDetails userDetails
    // ) {
    //     Long memberId = userDetails.getMemberId();
    //     List<UpcomingConcertResponse> result = concertService.getMyUpcomingConcerts(memberId);
    //     return ResponseEntity.ok()
    //             .body(Response.success(SuccessCode.GET_SUCCESS, result, "내 다가오는 공연 조회 성공"));
    // }
}


