package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/venues")
public interface VenueControllerDocs {

    // 공연장 검색
    @Operation(
            summary = "공연장 검색",
            description = "검색어를 포함하는 공연장을 조회하여 Pagination으로 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색어를 다시 확인해주세요")
    })
    @GetMapping("/search")
    ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> searchVenues(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") Integer page
    );

    // 인기 검색 공연장 조회
    @Operation(
            summary = "인기 검색 공연장 조회",
            description = "검색 횟수가 가장 많은 공연장 5개를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/trend-search")
    ResponseEntity<Response<List<VenueResDTO.VenuePreviewDTO>>> trendSearchVenues();

    // 즐겨찾기 공연장 등록
    @Operation(
            summary = "즐겨찾기 공연장 등록",
            description = "해당 공연장을 즐겨찾기 목록에 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다.")
    })
    @PostMapping("/favorites/{venueId}")
    ResponseEntity<Response<VenueResDTO.VenueSimpleDTO>> addFavoriteVenue(
            @PathVariable("venueId") Long venueId,
            @RequestHeader("X-Member-Id") Long memberId // 임시 사용자
    );

}
