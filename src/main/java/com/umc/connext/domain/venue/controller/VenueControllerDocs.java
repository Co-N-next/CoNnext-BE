package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}