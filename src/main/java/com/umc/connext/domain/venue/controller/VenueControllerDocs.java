package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/venues")
public interface VenueControllerDocs {

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
