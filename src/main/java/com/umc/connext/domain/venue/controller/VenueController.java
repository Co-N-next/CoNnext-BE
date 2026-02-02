package com.umc.connext.domain.venue.controller;

import com.umc.connext.common.code.SuccessCode;
import com.umc.connext.common.response.Response;
import com.umc.connext.domain.venue.dto.VenueResponse;
import com.umc.connext.domain.venue.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Venue Map", description = "공연장 지도/맵 데이터 조회 API")
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @Operation(
            summary = "공연장 전체 맵 데이터 조회",
            description = "공연장 지도 데이터를 조회합니다. (층별 구역 좌표 + 시설물 좌표) 일반적으로 최초 1회 호출 후 캐싱하여 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "404", description = "공연장 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{venueId}/map")
    public ResponseEntity<Response<VenueResponse>> getVenueMap(
            @Parameter(description = "공연장 ID", example = "1", required = true)
            @PathVariable Long venueId
    ) {
        VenueResponse mapData = venueService.getVenueMap(venueId);
        return ResponseEntity.ok(Response.success(SuccessCode.GET_SUCCESS, mapData));
    }
}
